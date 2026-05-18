package com.umc.product.test.application.service;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.test.application.port.in.command.SeedProjectsUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult.PartialProject;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult.SkippedCell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 프로젝트 시딩 서비스. ADR-017 참조.
 * <p>
 * 활성 기수의 (Chapter, School) 셀을 School 단위로 round-robin 순회하면서 같은 school 멤버 풀에서
 * 슬롯 총원을 무작위 추출해 한 프로젝트를 구성한다. PO 후보가 PLAN 챌린저로 등록되지 않은 경우
 * 시딩 측에서 PLAN 챌린저로 등록한 뒤 createDraft 를 호출한다. 같은 호출 안에서 사용된 Member 는
 * 다른 프로젝트에 재배정되지 않는다(uk_project_member_project_member 위반 방지).
 * <p>
 * <b>부분 성공 처리</b>: createDraft 성공 후 일부 addProjectMember 가 실패하더라도 프로젝트는
 * 잔존하므로(create 와 add 가 별도 트랜잭션), 이 경우는 {@code partialProjects} 로 응답에 명시한다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProjectSeedService implements SeedProjectsUseCase {

    private final PartAssignmentPolicy partAssignmentPolicy;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final CreateDraftProjectUseCase createDraftProjectUseCase;
    private final AddProjectMemberUseCase addProjectMemberUseCase;

    @Override
    public SeedProjectsResult seed(SeedProjectsCommand command) {
        Long gisuId = command.gisuId() != null ? command.gisuId() : getGisuUseCase.getActiveGisuId();
        int projectCount = command.projectCount();

        long startedAt = System.currentTimeMillis();
        log.info("project seed start: projectCount={}, gisuId={}", projectCount, gisuId);

        List<SchoolCell> cells = flattenSchoolCells(gisuId);

        List<Long> createdProjectIds = new ArrayList<>();
        List<PartialProject> partialProjects = new ArrayList<>();
        List<SkippedCell> skipped = new ArrayList<>();
        Set<Long> usedMemberIds = new HashSet<>();
        int failedCount = 0;

        int index = 0;
        int totalCells = cells.size();
        int consecutiveSkipsOnFullRound = 0;
        int finalizedCount = 0;

        while (finalizedCount < projectCount && totalCells > 0 && consecutiveSkipsOnFullRound < totalCells) {
            SchoolCell cell = cells.get(index % totalCells);
            index++;

            SeedAttemptResult attempt = trySeedOneProject(gisuId, cell, usedMemberIds);
            switch (attempt.outcome()) {
                case CREATED -> {
                    createdProjectIds.add(attempt.projectId());
                    finalizedCount++;
                    consecutiveSkipsOnFullRound = 0;
                }
                case PARTIAL -> {
                    partialProjects.add(new PartialProject(
                        attempt.projectId(),
                        cell.chapterId(),
                        cell.schoolId(),
                        attempt.addedMemberCount(),
                        attempt.expectedMemberCount(),
                        attempt.reason()
                    ));
                    finalizedCount++;
                    consecutiveSkipsOnFullRound = 0;
                }
                case SKIPPED -> {
                    skipped.add(new SkippedCell(cell.chapterId(), cell.schoolId(), attempt.reason()));
                    consecutiveSkipsOnFullRound++;
                }
                case FAILED -> {
                    failedCount++;
                    consecutiveSkipsOnFullRound++;
                }
            }
        }

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info(
            "project seed completed in {}ms: created={}, partial={}, skipped={}, failed={}",
            elapsedMs, createdProjectIds.size(), partialProjects.size(), skipped.size(), failedCount
        );

        return new SeedProjectsResult(createdProjectIds, partialProjects, skipped, failedCount);
    }

    private List<SchoolCell> flattenSchoolCells(Long gisuId) {
        List<ChapterWithSchoolsInfo> chapters = getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId);
        List<SchoolCell> result = new ArrayList<>();
        for (ChapterWithSchoolsInfo chapter : chapters) {
            for (ChapterWithSchoolsInfo.SchoolInfo school : chapter.schools()) {
                result.add(new SchoolCell(chapter.chapterId(), school.schoolId()));
            }
        }
        return result;
    }

    private SeedAttemptResult trySeedOneProject(Long gisuId, SchoolCell cell, Set<Long> usedMemberIds) {
        Set<Long> rawPool = getMemberUseCase.findAllIdsBySchoolId(cell.schoolId());
        List<Long> availablePool = rawPool.stream().filter(id -> !usedMemberIds.contains(id)).toList();

        List<ChallengerPart> slots = partAssignmentPolicy.nextProjectSlots(availablePool.size());
        if (slots.isEmpty()) {
            return SeedAttemptResult.skipped(
                "INSUFFICIENT_POOL (need>=%d, available=%d)".formatted(PartAssignmentPolicy.MIN_TOTAL, availablePool.size())
            );
        }

        List<Long> shuffled = new ArrayList<>(availablePool);
        Collections.shuffle(shuffled);
        List<Long> picked = shuffled.subList(0, slots.size());

        Optional<Long> poCandidate = pickPoMemberId(picked, gisuId);
        if (poCandidate.isEmpty()) {
            return SeedAttemptResult.skipped("NO_PO_CANDIDATE (all picked members already have non-PLAN challenger in gisu %d)"
                .formatted(gisuId));
        }
        Long poMemberId = poCandidate.get();

        Long projectId;
        try {
            ensurePlanChallenger(poMemberId, gisuId);
            projectId = createDraftProjectUseCase.create(CreateDraftProjectCommand.builder()
                .gisuId(gisuId)
                .productOwnerMemberId(poMemberId)
                .requesterMemberId(poMemberId)
                .build());
        } catch (Exception e) {
            log.error(
                "project seed failed at create (chapterId={}, schoolId={}, poMemberId={}): {}",
                cell.chapterId(), cell.schoolId(), poMemberId, e.toString()
            );
            return SeedAttemptResult.failed(e.toString());
        }

        int addedCount = 0;
        String firstAddFailureReason = null;
        for (int i = 0; i < slots.size(); i++) {
            Long memberId = picked.get(i);
            ChallengerPart part = slots.get(i);
            try {
                addProjectMemberUseCase.add(AddProjectMemberCommand.builder()
                    .projectId(projectId)
                    .memberId(memberId)
                    .part(part)
                    .requesterMemberId(poMemberId)
                    .build());
                usedMemberIds.add(memberId);
                addedCount++;
            } catch (Exception e) {
                if (firstAddFailureReason == null) {
                    firstAddFailureReason = "memberId=%d, part=%s, error=%s".formatted(memberId, part, e);
                }
                log.error(
                    "project seed addMember failed (projectId={}, memberId={}, part={}): {}",
                    projectId, memberId, part, e.toString()
                );
            }
        }

        if (addedCount == slots.size()) {
            return SeedAttemptResult.created(projectId);
        }
        return SeedAttemptResult.partial(projectId, addedCount, slots.size(), firstAddFailureReason);
    }

    /**
     * 슬롯 멤버 중 PO 가 될 수 있는 첫 멤버를 찾는다. 챌린저 미등록 또는 이미 PLAN 챌린저인 멤버가
     * 후보. 이미 다른 파트로 등록된 멤버는 후보 부적합 (한 멤버당 (gisu, part) 1건 unique).
     */
    private Optional<Long> pickPoMemberId(List<Long> picked, Long gisuId) {
        for (Long memberId : picked) {
            Optional<ChallengerInfo> existing = getChallengerUseCase.findByMemberIdAndGisuId(memberId, gisuId);
            if (existing.isEmpty() || existing.get().part() == ChallengerPart.PLAN) {
                return Optional.of(memberId);
            }
        }
        return Optional.empty();
    }

    /**
     * PO 가 PLAN 챌린저로 등록되어 있지 않으면 새로 등록한다. 이미 PLAN 챌린저면 그대로 둔다.
     */
    private void ensurePlanChallenger(Long memberId, Long gisuId) {
        Optional<ChallengerInfo> existing = getChallengerUseCase.findByMemberIdAndGisuId(memberId, gisuId);
        if (existing.isPresent() && existing.get().part() == ChallengerPart.PLAN) {
            return;
        }
        manageChallengerUseCase.createChallenger(CreateChallengerCommand.builder()
            .memberId(memberId)
            .part(ChallengerPart.PLAN)
            .gisuId(gisuId)
            .build());
    }

    private record SchoolCell(Long chapterId, Long schoolId) {
    }

    private record SeedAttemptResult(
        Outcome outcome,
        Long projectId,
        int addedMemberCount,
        int expectedMemberCount,
        String reason
    ) {

        enum Outcome { CREATED, PARTIAL, SKIPPED, FAILED }

        static SeedAttemptResult created(Long projectId) {
            return new SeedAttemptResult(Outcome.CREATED, projectId, 0, 0, null);
        }

        static SeedAttemptResult partial(Long projectId, int added, int expected, String reason) {
            return new SeedAttemptResult(Outcome.PARTIAL, projectId, added, expected, reason);
        }

        static SeedAttemptResult skipped(String reason) {
            return new SeedAttemptResult(Outcome.SKIPPED, null, 0, 0, reason);
        }

        static SeedAttemptResult failed(String reason) {
            return new SeedAttemptResult(Outcome.FAILED, null, 0, 0, reason);
        }
    }
}
