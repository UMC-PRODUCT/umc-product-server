package com.umc.product.test.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.UpsertProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand;
import com.umc.product.project.application.port.in.query.GetProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.ChapterOption;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.SchoolOption;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.test.application.port.in.command.SeedProjectScenariosUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult.CreatedProject;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult.FailedProject;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult.PartQuota;
import com.umc.product.test.application.port.in.command.dto.TargetProjectStatus;
import com.umc.product.test.application.service.ChapterQuotaBudget.Allocation.Allocated;
import com.umc.product.test.application.service.ChapterQuotaBudget.Allocation.Exhausted;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시나리오 기반 프로젝트 시딩 서비스.
 * <p>
 * 활성 기수에 대해 {@link TargetProjectStatus} 까지 도달한 프로젝트를 N 개 생성한다. SQL 직접 주입이
 * 아니라 도메인 UseCase 시퀀스를 호출해 만들기 때문에 도메인 가드를 모두 통과한 데이터가 된다.
 * <ul>
 *   <li>{@code DRAFT}          — PO 가 createDraft + updateProject(name/desc)</li>
 *   <li>{@code PENDING_REVIEW} — 위 + PO 가 upsertForm + submit</li>
 *   <li>{@code IN_PROGRESS}    — 운영진이 PO 를 지정해 createDraft + updateProject + upsertForm +
 *       updatePartQuotas(DRAFT) + submit. 운영진의 submit 은 곧바로 공개까지 진행한다</li>
 * </ul>
 * <p>
 * IN_PROGRESS 의 TO 는 운영진이 관리하는 지부별 {@link ChapterQuotaBudget} 에서 고른다. 지부에 여유가 없으면
 * 프로젝트를 만들지 않고 {@code QUOTA} 실패로 보고한다. 팀원은 채우지 않는다.
 * <p>
 * <b>트랜잭션 정책</b>: 외부 트랜잭션 차단({@link Propagation#NOT_SUPPORTED}). 각 단계 UseCase 가
 * 자체 트랜잭션을 가지므로 중간 단계가 실패해도 그 앞까지의 변경은 commit 된다. 호출자는
 * {@link FailedProject} 의 {@code reachedStatus} 로 orphan 정리 여부를 판단해야 한다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProjectScenarioSeedService implements SeedProjectScenariosUseCase {

    private final GetGisuUseCase getGisuUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetProjectQuotaManagementUseCase getProjectQuotaManagementUseCase;
    private final CreateDraftProjectUseCase createDraftProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final UpsertProjectApplicationFormUseCase upsertProjectApplicationFormUseCase;
    private final UpdatePartQuotasUseCase updatePartQuotasUseCase;
    private final SubmitProjectUseCase submitProjectUseCase;
    private final DummyProjectFactory dummyProjectFactory;
    private final DummyApplicationFormFactory dummyApplicationFormFactory;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SeedProjectScenariosResult seed(SeedProjectScenariosCommand command) {
        Long gisuId = getGisuUseCase.getActiveGisuId();
        long startedAt = System.currentTimeMillis();
        log.info(
            "project scenario seed start: targetStatus={}, projectCount={}, gisuId={}, adminMemberId={}",
            command.targetStatus(), command.projectCount(), gisuId, command.adminMemberId()
        );

        RandomGenerator random = ThreadLocalRandom.current();
        AdminScope adminScope = command.targetStatus() == TargetProjectStatus.IN_PROGRESS
            ? loadAdminScope(command.adminMemberId(), gisuId)
            : null;
        List<Long> poIds = resolvePOs(command, gisuId, adminScope, random);

        List<CreatedProject> created = new ArrayList<>();
        List<FailedProject> failed = new ArrayList<>();

        int seq = 1;
        for (Long poMemberId : poIds) {
            ScenarioOutcome outcome = runScenario(poMemberId, command.targetStatus(), gisuId, seq, adminScope, random);
            if (outcome.failure != null) {
                failed.add(outcome.failure);
            } else {
                created.add(outcome.success);
            }
            seq++;
        }

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info(
            "project scenario seed completed in {}ms: created={}, failed={}",
            elapsedMs, created.size(), failed.size()
        );

        return new SeedProjectScenariosResult(created, failed);
    }

    /**
     * 운영진이 TO 를 관리할 수 있는 지부와 지부별 TO 여유를 TO 관리 화면과 같은 UseCase 로 읽는다.
     * 운영진 권한이 없으면 그 UseCase 가 거부한다.
     */
    private AdminScope loadAdminScope(Long adminMemberId, Long gisuId) {
        ProjectQuotaManagementInfo overview = getProjectQuotaManagementUseCase.get(
            adminMemberId, gisuId, null, null, null);
        Map<Long, ChapterQuotaBudget> budgets = new HashMap<>();
        Set<Long> schoolIds = new HashSet<>();
        for (ChapterOption chapter : overview.chapters()) {
            ProjectQuotaManagementInfo info = getProjectQuotaManagementUseCase.get(
                adminMemberId, gisuId, chapter.chapterId(), null, null);
            budgets.put(chapter.chapterId(), ChapterQuotaBudget.from(info));
            info.schools().stream().map(SchoolOption::schoolId).forEach(schoolIds::add);
        }
        return new AdminScope(adminMemberId, budgets, schoolIds);
    }

    private List<Long> resolvePOs(SeedProjectScenariosCommand command, Long gisuId, AdminScope adminScope,
                                  RandomGenerator random) {
        List<Long> input = command.productOwnerMemberIds();
        int count = command.projectCount();

        if (input != null) {
            if (input.size() != count) {
                throw new IllegalArgumentException(
                    "productOwnerMemberIds size (%d) must equal projectCount (%d)"
                        .formatted(input.size(), count)
                );
            }
            validateAllArePlanChallengers(input, gisuId);
            return List.copyOf(input);
        }

        List<Long> pool = getChallengerUseCase.getAllByGisuId(gisuId).stream()
            .filter(c -> c.part() == ChallengerPart.PLAN)
            .filter(c -> c.challengerStatus() == ChallengerStatus.ACTIVE)
            .map(ChallengerInfo::memberId)
            .toList();
        if (adminScope != null && !pool.isEmpty()) {
            Map<Long, Long> schoolIdByMemberId = getMemberUseCase.findAllSchoolIdsByIds(new HashSet<>(pool));
            pool = pool.stream()
                .filter(memberId -> adminScope.schoolIds().contains(schoolIdByMemberId.get(memberId)))
                .toList();
        }
        if (pool.size() < count) {
            throw new IllegalArgumentException(
                "active PLAN challenger pool size (%d) is less than projectCount (%d)"
                    .formatted(pool.size(), count)
            );
        }
        List<Long> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, count));
    }

    private void validateAllArePlanChallengers(List<Long> memberIds, Long gisuId) {
        Map<Long, ChallengerInfo> map = getChallengerUseCase.listByMemberIdsAndGisuId(
            new HashSet<>(memberIds), gisuId
        );
        for (Long memberId : memberIds) {
            ChallengerInfo c = map.get(memberId);
            if (c == null) {
                throw new IllegalArgumentException(
                    "memberId=%d is not a challenger of gisuId=%d".formatted(memberId, gisuId)
                );
            }
            if (c.part() != ChallengerPart.PLAN) {
                throw new IllegalArgumentException(
                    "memberId=%d is not a PLAN challenger (actual=%s)".formatted(memberId, c.part())
                );
            }
        }
    }

    private ScenarioOutcome runScenario(Long poMemberId, TargetProjectStatus target, Long gisuId, int seq,
                                        AdminScope adminScope, RandomGenerator random) {
        Long requesterId = adminScope == null ? poMemberId : adminScope.adminMemberId();

        Long schoolId;
        Long chapterId;
        try {
            schoolId = getMemberUseCase.getById(poMemberId).schoolId();
            chapterId = getChapterUseCase.byGisuAndSchool(gisuId, schoolId).id();
        } catch (Exception e) {
            return ScenarioOutcome.failure(failureOf(null, null, target, "CREATE_DRAFT", e.toString()));
        }

        List<Entry> quotas = List.of();
        if (adminScope != null) {
            ChapterQuotaBudget budget = adminScope.budgets().get(chapterId);
            if (budget == null) {
                return ScenarioOutcome.failure(failureOf(null, null, target, "QUOTA",
                    "adminMemberId=%d cannot manage chapterId=%d of PO memberId=%d"
                        .formatted(requesterId, chapterId, poMemberId)));
            }
            switch (budget.allocate(random)) {
                case Allocated allocated -> quotas = allocated.entries();
                case Exhausted exhausted -> {
                    return ScenarioOutcome.failure(
                        failureOf(null, null, target, "QUOTA", exhausted.reason()));
                }
            }
        }

        Long projectId;
        try {
            projectId = createDraftProjectUseCase.create(CreateDraftProjectCommand.builder()
                .gisuId(gisuId)
                .productOwnerMemberId(poMemberId)
                .requesterMemberId(requesterId)
                .build());
        } catch (Exception e) {
            return ScenarioOutcome.failure(failureOf(null, null, target, "CREATE_DRAFT", e.toString()));
        }
        TargetProjectStatus reached = TargetProjectStatus.DRAFT;

        try {
            updateProjectUseCase.update(UpdateProjectCommand.builder()
                .projectId(projectId)
                .requesterMemberId(requesterId)
                .name(dummyProjectFactory.nextName(seq))
                .description(dummyProjectFactory.nextDescription(seq))
                .build());
        } catch (Exception e) {
            return ScenarioOutcome.failure(failureOf(projectId, reached, target, "UPDATE", e.toString()));
        }

        if (target == TargetProjectStatus.DRAFT) {
            return ScenarioOutcome.success(new CreatedProject(
                projectId, reached, poMemberId, chapterId, schoolId, null, List.of()
            ));
        }

        Long applicationFormId;
        try {
            applicationFormId = upsertProjectApplicationFormUseCase.upsert(
                UpsertApplicationFormCommand.builder()
                    .projectId(projectId)
                    .requesterMemberId(requesterId)
                    .title(null)
                    .description("시딩 더미 지원서")
                    .sections(dummyApplicationFormFactory.defaultSections())
                    .build()
            ).applicationFormId();
        } catch (Exception e) {
            return ScenarioOutcome.failure(failureOf(projectId, reached, target, "FORM", e.toString()));
        }

        if (!quotas.isEmpty()) {
            try {
                updatePartQuotasUseCase.update(UpdatePartQuotasCommand.builder()
                    .projectId(projectId)
                    .entries(quotas)
                    .requesterMemberId(requesterId)
                    .build());
            } catch (Exception e) {
                return ScenarioOutcome.failure(failureOf(projectId, reached, target, "QUOTA", e.toString()));
            }
        }

        ProjectStatus submitted;
        try {
            submitted = submitProjectUseCase.submit(SubmitProjectCommand.builder()
                .projectId(projectId)
                .requesterMemberId(requesterId)
                .build());
        } catch (Exception e) {
            return ScenarioOutcome.failure(failureOf(projectId, reached, target, "SUBMIT", e.toString()));
        }
        reached = TargetProjectStatus.valueOf(submitted.name());
        if (reached != target) {
            return ScenarioOutcome.failure(failureOf(projectId, reached, target, "SUBMIT",
                "submit by memberId=%d ended in %s".formatted(requesterId, submitted)));
        }

        return ScenarioOutcome.success(new CreatedProject(
            projectId, reached, poMemberId, chapterId, schoolId, applicationFormId,
            quotas.stream().map(entry -> new PartQuota(entry.part(), entry.quota())).toList()
        ));
    }

    private FailedProject failureOf(Long projectId, TargetProjectStatus reached,
                                    TargetProjectStatus intended, String step, String reason) {
        log.error(
            "project scenario seed step failed (step={}, projectId={}, reached={}, intended={}): {}",
            step, projectId, reached, intended, reason
        );
        return new FailedProject(projectId, reached, intended, step, reason);
    }

    private record AdminScope(Long adminMemberId, Map<Long, ChapterQuotaBudget> budgets, Set<Long> schoolIds) {
    }

    private record ScenarioOutcome(CreatedProject success, FailedProject failure) {
        static ScenarioOutcome success(CreatedProject p) {
            return new ScenarioOutcome(p, null);
        }

        static ScenarioOutcome failure(FailedProject f) {
            return new ScenarioOutcome(null, f);
        }
    }
}
