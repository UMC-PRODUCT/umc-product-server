package com.umc.product.test.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.DecideApplicationUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectApplicationUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectApplicationDraftUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectApplicationCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;
import com.umc.product.project.application.port.in.query.GetProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.test.application.port.in.command.SeedProjectApplicationsUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsResult;
import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsResult.FailedApplication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지원서 시나리오 시딩 서비스.
 * <p>
 * 지정 매칭 차수 + 지부를 기준으로, 아직 팀에 합류하지 않은 ACTIVE 챌린저들이 랜덤 프로젝트에 Draft 생성 → 더미 답변 → Submit → 합불 결정 → APPROVED 시 ProjectMember
 * 등록까지 실행한다.
 * <p>
 * <b>전제 조건</b>: 매칭차수가 현재 OPEN 상태(startsAt <= now <= endsAt)여야 한다.
 * <p>
 * <b>트랜잭션 정책</b>: {@link Propagation#NOT_SUPPORTED}. 각 UseCase 가 자체 트랜잭션을 가지므로
 * 한 챌린저 시나리오가 실패해도 이전 성공 건은 commit 된다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProjectApplicationSeedService implements SeedProjectApplicationsUseCase {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetProjectMatchingRoundUseCase getProjectMatchingRoundUseCase;
    private final GetProjectApplicationFormUseCase getProjectApplicationFormUseCase;
    private final GetProjectUseCase getProjectUseCase;
    private final GetGisuUseCase getGisuUseCase;

    private final CreateDraftProjectApplicationUseCase createDraftProjectApplicationUseCase;
    private final UpdateProjectApplicationDraftUseCase updateProjectApplicationDraftUseCase;
    private final SubmitProjectApplicationUseCase submitProjectApplicationUseCase;
    private final DecideApplicationUseCase decideApplicationUseCase;
    private final AddProjectMemberUseCase addProjectMemberUseCase;

    // 시딩 서비스 특성상 통계/조회용 Port 직접 주입 (UseCase 미존재 or 과도한 데이터 로딩 방지)
    private final LoadProjectStatisticsPort loadProjectStatisticsPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;

    @Override
    public SeedProjectApplicationsResult seed(SeedProjectApplicationsCommand command) {
        long startedAt = System.currentTimeMillis();

        // 1. 매칭차수 조회 - chapterId 소속 여부 검증 및 type 파악
        ProjectMatchingRoundInfo round = getProjectMatchingRoundUseCase
            .list(command.chapterId(), null).stream()
            .filter(r -> r.id().equals(command.matchingRoundId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "matchingRoundId=%d 가 chapterId=%d 에 속하지 않습니다."
                    .formatted(command.matchingRoundId(), command.chapterId())
            ));

        // 2. 지부 내 IN_PROGRESS 프로젝트 목록
        List<ProjectStatisticsProjectRow> projects =
            loadProjectStatisticsPort.listProjectsByChapterId(command.chapterId());
        if (projects.isEmpty()) {
            log.warn("project application seed skipped: chapterId={} 에 IN_PROGRESS 프로젝트가 없습니다.",
                command.chapterId());
            return new SeedProjectApplicationsResult(0, 0, 0, List.of());
        }

        Set<Long> projectIds = projects.stream()
            .map(ProjectStatisticsProjectRow::projectId)
            .collect(Collectors.toSet());
        Long gisuId = getGisuUseCase.getActiveGisuId();

        // 3. 프로젝트별 PO memberId 사전 캐시 (AddProjectMemberUseCase requesterMemberId 용)
        Map<Long, Long> ownerByProjectId = projectIds.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> getProjectUseCase.getById(id).productOwnerMemberId()
            ));

        // 4. 프로젝트별 파트 TO 사전 캐시
        Map<Long, Map<ChallengerPart, Long>> quotaByProject =
            loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds).entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().collect(
                        Collectors.toMap(ProjectPartQuota::getPart, ProjectPartQuota::getQuota))
                ));

        // 4-1. 프로젝트별·파트별 현재 ACTIVE 멤버 수 캐시 (쿼터 초과 방지용, 시딩 중 인메모리 갱신)
        Map<Long, Map<ChallengerPart, Long>> currentMemberCounts =
            loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(projectIds).entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new HashMap<>(e.getValue()),
                    (a, b) -> a,
                    HashMap::new
                ));

        // 5. 이미 해당 차수에 지원한 memberId Set (중복 지원 방지)
        Set<Long> alreadyApplied = loadProjectApplicationPort
            .listByMatchingRoundId(command.matchingRoundId()).stream()
            .map(ProjectApplication::getApplicantMemberId)
            .collect(Collectors.toSet());

        // 6. 매칭차수 type에 맞는 ACTIVE 챌린저 풀 구성 (이미 팀원이거나 이미 지원한 챌린저 제외)
        Set<ChallengerPart> eligibleParts = eligibleParts(round.type());
        List<ChallengerInfo> challengers = getChallengerUseCase.getAllByGisuId(gisuId).stream()
            .filter(c -> c.challengerStatus() == ChallengerStatus.ACTIVE)
            .filter(c -> eligibleParts.contains(c.part()))
            .filter(c -> !alreadyApplied.contains(c.memberId()))
            .filter(c -> !loadProjectMemberPort.existsByGisuAndMember(gisuId, c.memberId()))
            .toList();

        log.info(
            "project application seed start: chapterId={}, matchingRoundId={}, roundType={}, "
                + "projectCount={}, challengerPoolSize={}",
            command.chapterId(), command.matchingRoundId(), round.type(),
            projects.size(), challengers.size()
        );

        // 7. 챌린저마다 시나리오 실행
        List<FailedApplication> failures = new ArrayList<>();
        int submittedCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        for (ChallengerInfo challenger : challengers) {
            // 쿼터 여유분이 있는 프로젝트만 후보로 포함 (현재 ACTIVE 수 < 쿼터)
            List<Long> candidates = projectIds.stream()
                .filter(pid -> {
                    Long quota = quotaByProject.getOrDefault(pid, Map.of()).get(challenger.part());
                    if (quota == null) {
                        return false;
                    }
                    long current = currentMemberCounts
                        .getOrDefault(pid, Map.of())
                        .getOrDefault(challenger.part(), 0L);
                    return current < quota;
                })
                .filter(pid -> !ownerByProjectId.get(pid).equals(challenger.memberId()))
                .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                failures.add(new FailedApplication(
                    challenger.memberId(), null, "NO_PROJECT",
                    "파트=%s 를 모집하는 프로젝트가 없거나 모든 프로젝트의 쿼터가 소진되었습니다.".formatted(challenger.part())
                ));
                continue;
            }

            Collections.shuffle(candidates);
            Long projectId = candidates.get(0);
            Long poMemberId = ownerByProjectId.get(projectId);

            ApplicationOutcome outcome = runApplicationScenario(
                challenger, projectId, poMemberId, command.matchingRoundId(), command.approveRatio()
            );

            switch (outcome.step()) {
                case "SUBMITTED" -> submittedCount++;
                case "APPROVED" -> {
                    submittedCount++;
                    approvedCount++;
                    // ADD_MEMBER 성공 → 인메모리 카운트 갱신 (다음 챌린저의 쿼터 여유분 계산에 반영)
                    currentMemberCounts
                        .computeIfAbsent(projectId, k -> new HashMap<>())
                        .merge(challenger.part(), 1L, Long::sum);
                }
                case "REJECTED" -> {
                    submittedCount++;
                    rejectedCount++;
                }
                default -> failures.add(new FailedApplication(
                    challenger.memberId(), projectId, outcome.step(), outcome.reason()
                ));
            }
        }

        long elapsed = System.currentTimeMillis() - startedAt;
        log.info(
            "project application seed completed in {}ms: submitted={}, approved={}, rejected={}, failed={}",
            elapsed, submittedCount, approvedCount, rejectedCount, failures.size()
        );

        return new SeedProjectApplicationsResult(submittedCount, approvedCount, rejectedCount, failures);
    }

    private ApplicationOutcome runApplicationScenario(
        ChallengerInfo challenger, Long projectId, Long poMemberId,
        Long matchingRoundId, Double approveRatio
    ) {
        // DRAFT 생성
        Long applicationId;
        try {
            applicationId = createDraftProjectApplicationUseCase.create(
                CreateDraftProjectApplicationCommand.builder()
                    .projectId(projectId)
                    .applicantMemberId(challenger.memberId())
                    .matchingRoundId(matchingRoundId)
                    .build()
            ).applicationId();
        } catch (Exception e) {
            log.error("seed DRAFT 실패 (memberId={}, projectId={}): {}",
                challenger.memberId(), projectId, e.toString());
            return ApplicationOutcome.fail("DRAFT", e.toString());
        }

        // 더미 답변 채우기 (isRequired=true 질문 대응)
        try {
            List<UpdateProjectApplicationDraftCommand.AnswerEntry> answers =
                buildDummyAnswers(projectId, challenger.memberId());
            if (!answers.isEmpty()) {
                updateProjectApplicationDraftUseCase.update(
                    UpdateProjectApplicationDraftCommand.builder()
                        .projectId(projectId)
                        .requesterMemberId(challenger.memberId())
                        .answers(answers)
                        .build()
                );
            }
        } catch (Exception e) {
            log.error("seed FILL 실패 (memberId={}, projectId={}): {}",
                challenger.memberId(), projectId, e.toString());
            return ApplicationOutcome.fail("FILL", e.toString());
        }

        // 제출
        try {
            submitProjectApplicationUseCase.submit(
                SubmitProjectApplicationCommand.builder()
                    .projectId(projectId)
                    .requesterMemberId(challenger.memberId())
                    .build()
            );
        } catch (Exception e) {
            log.error("seed SUBMIT 실패 (memberId={}, projectId={}): {}",
                challenger.memberId(), projectId, e.toString());
            return ApplicationOutcome.fail("SUBMIT", e.toString());
        }

        // 합불 결정
        boolean approve = ThreadLocalRandom.current().nextDouble() < approveRatio;
        ApplicationDecisionStatus decision =
            approve ? ApplicationDecisionStatus.APPROVED : ApplicationDecisionStatus.REJECTED;
        try {
            decideApplicationUseCase.decide(applicationId, decision, null, poMemberId);
        } catch (Exception e) {
            log.error("seed DECIDE 실패 (memberId={}, projectId={}, decision={}): {}",
                challenger.memberId(), projectId, decision, e.toString());
            return ApplicationOutcome.fail("DECIDE", e.toString());
        }

        if (!approve) {
            return ApplicationOutcome.success("REJECTED");
        }

        // APPROVED → ProjectMember 추가 (통계의 APPROVED + ACTIVE 교집합 조건 충족용)
        try {
            addProjectMemberUseCase.add(
                AddProjectMemberCommand.builder()
                    .projectId(projectId)
                    .memberId(challenger.memberId())
                    .part(challenger.part())
                    .requesterMemberId(poMemberId)
                    .build()
            );
        } catch (Exception e) {
            log.error("seed ADD_MEMBER 실패 (memberId={}, projectId={}): {}",
                challenger.memberId(), projectId, e.toString());
            return ApplicationOutcome.fail("ADD_MEMBER", e.toString());
        }

        return ApplicationOutcome.success("APPROVED");
    }

    /**
     * isRequired=true 인 질문만 추려 더미 텍스트 답변을 구성한다. 챌린저 시점으로 form 조회 — COMMON 섹션 + 본인 파트 PART 섹션만 노출되어 마스킹된 섹션은 자동 제외된다.
     */
    private List<UpdateProjectApplicationDraftCommand.AnswerEntry> buildDummyAnswers(
        Long projectId, Long challengerMemberId
    ) {
        return getProjectApplicationFormUseCase
            .findByProjectId(projectId, challengerMemberId)
            .map(form -> form.sections().stream()
                .flatMap(s -> s.questions().stream())
                .filter(ApplicationFormInfo.QuestionInfo::isRequired)
                .map(q -> UpdateProjectApplicationDraftCommand.AnswerEntry.builder()
                    .questionId(q.questionId())
                    .textValue("[시딩 더미 답변]")
                    .selectedOptionIds(List.of())
                    .fileIds(List.of())
                    .build())
                .toList())
            .orElse(List.of());
    }

    private Set<ChallengerPart> eligibleParts(MatchingType type) {
        if (type == MatchingType.PLAN_DESIGN) {
            return EnumSet.of(ChallengerPart.DESIGN);
        }
        return EnumSet.of(
            ChallengerPart.WEB,
            ChallengerPart.ANDROID,
            ChallengerPart.IOS,
            ChallengerPart.NODEJS,
            ChallengerPart.SPRINGBOOT
        );
    }

    private record ApplicationOutcome(String step, String reason) {

        static ApplicationOutcome success(String step) {
            return new ApplicationOutcome(step, null);
        }

        static ApplicationOutcome fail(String step, String reason) {
            return new ApplicationOutcome(step, reason);
        }
    }
}
