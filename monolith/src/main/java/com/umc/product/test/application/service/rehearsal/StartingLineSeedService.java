package com.umc.product.test.application.service.rehearsal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.UpsertProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand;
import com.umc.product.test.application.port.in.command.SeedStartingLineUseCase;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult.ChapterLine;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult.ProjectOutcome;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult.Role;
import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.application.service.DummyApplicationFormFactory;
import com.umc.product.test.application.service.qa.SeedFailureCode;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.exception.SeedRehearsalException;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;
import com.umc.product.test.domain.startingline.OwnedProject;
import com.umc.product.test.domain.startingline.ProjectQuota;
import com.umc.product.test.domain.startingline.QuotaApportionment;
import com.umc.product.test.domain.startingline.StartingLinePlan;
import com.umc.product.test.domain.startingline.StartingLineStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지부마다 QA 프로젝트 2개는 제출까지, 나머지 PLAN 프로젝트는 TO를 넣고 공개까지 만든다. 마지막 PLAN 2명은 프로젝트 없이 둔다.
 * <p>
 * 트랜잭션을 열지 않아 단계마다 운영 UseCase가 각자 커밋한다. PO마다 지금 상태에서 남은 단계만 실행하므로 다시 부르면
 * 끝난 프로젝트는 건너뛰고 멈춘 프로젝트는 이어서 끝낸다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RequiredArgsConstructor
public class StartingLineSeedService implements SeedStartingLineUseCase {

    private static final int MAX_STEPS = StartingLineStep.values().length;

    private final RehearsalContextLoader contextLoader;
    private final GetMemberUseCase getMemberUseCase;
    private final SeedProjectReadPort seedProjectReadPort;
    private final CreateDraftProjectUseCase createDraftProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final UpsertProjectApplicationFormUseCase upsertProjectApplicationFormUseCase;
    private final SubmitProjectUseCase submitProjectUseCase;
    private final UpdatePartQuotasUseCase updatePartQuotasUseCase;
    private final PublishProjectUseCase publishProjectUseCase;
    private final DummyApplicationFormFactory dummyApplicationFormFactory;

    @Override
    public StartingLineResult seed() {
        RehearsalContext context = contextLoader.load();
        RehearsalCast cast = context.cast();
        List<StartingLinePlan> plans = cast.chapters().stream().map(cast::startingLine).toList();
        Map<String, Long> memberIds = lookupAccounts(context, plans);
        Long superadminId = memberIds.get(context.superadminEmail());
        Map<Long, OwnedProject> owned = seedProjectReadPort.listOwnedProjects(context.gisuId(), memberIds.values())
            .stream().collect(Collectors.toMap(OwnedProject::ownerMemberId, Function.identity()));

        List<ChapterLine> chapters = new ArrayList<>();
        List<ProjectOutcome> projects = new ArrayList<>();
        for (StartingLinePlan plan : plans) {
            Map<ChallengerPart, Integer> headcount = cast.matchingHeadcount(plan.chapter());
            Map<RosterKey, ProjectQuota> quotas = QuotaApportionment.apportion(
                headcount, plan.seededOwners(), plan.qaOwners().size() + plan.withoutProject().size());
            chapters.add(chapterLine(context, plan, headcount, quotas));
            for (RosterKey owner : plan.qaOwners()) {
                projects.add(drive(context, plan.chapter(), owner, Role.QA, null, memberIds, superadminId, owned));
            }
            for (RosterKey owner : plan.seededOwners()) {
                projects.add(drive(context, plan.chapter(), owner, Role.SEEDED, quotas.get(owner), memberIds,
                    superadminId, owned));
            }
        }
        return new StartingLineResult(chapters, projects);
    }

    private Map<String, Long> lookupAccounts(RehearsalContext context, List<StartingLinePlan> plans) {
        Set<String> emails = new HashSet<>();
        emails.add(context.superadminEmail());
        plans.forEach(plan -> {
            plan.qaOwners().forEach(owner -> emails.add(context.email(owner)));
            plan.seededOwners().forEach(owner -> emails.add(context.email(owner)));
        });
        Map<String, Long> memberIds = getMemberUseCase.findIdsByEmails(emails);
        if (memberIds.size() != emails.size()) {
            throw new SeedRehearsalException(SeedErrorCode.ACCOUNT_NOT_SEEDED,
                "출발선에 필요한 계정 " + emails.size() + "개 중 " + (emails.size() - memberIds.size()) + "개가 없습니다.");
        }
        return memberIds;
    }

    private ChapterLine chapterLine(RehearsalContext context, StartingLinePlan plan,
                                    Map<ChallengerPart, Integer> headcount, Map<RosterKey, ProjectQuota> quotas) {
        Map<ChallengerPart, Integer> totals = new EnumMap<>(ChallengerPart.class);
        RehearsalCast.MATCHING_PARTS.forEach(part -> totals.put(part, 0));
        quotas.values().forEach(quota -> quota.entries().forEach((part, value) -> totals.merge(part, value, Integer::sum)));
        Map<ChallengerPart, Integer> reserved = new EnumMap<>(ChallengerPart.class);
        totals.forEach((part, total) -> reserved.put(part, headcount.getOrDefault(part, 0) - total));
        return new ChapterLine(plan.chapter(), plan.withoutProject().stream().map(context::email).toList(),
            plan.qaOwners().size(), plan.seededOwners().size(), totals, reserved);
    }

    private ProjectOutcome drive(RehearsalContext context, String chapter, RosterKey owner, Role role,
                                 ProjectQuota quota, Map<String, Long> memberIds, Long superadminId,
                                 Map<Long, OwnedProject> owned) {
        String email = context.email(owner);
        Long ownerId = memberIds.get(email);
        OwnedProject current = owned.get(ownerId);
        List<StartingLineStep> applied = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_STEPS; attempt++) {
            Optional<StartingLineStep> next = StartingLineStep.next(current, quota);
            if (next.isEmpty()) {
                break;
            }
            StartingLineStep step = next.get();
            try {
                current = execute(step, current, context, owner, role, quota, ownerId, superadminId);
                applied.add(step);
            } catch (RuntimeException e) {
                log.warn("출발선 단계 실패: owner={}, step={}", email, step, e);
                return new ProjectOutcome(chapter, email, role, current == null ? null : current.projectId(),
                    current == null ? null : current.status(), applied, step, SeedFailureCode.of(e), e.getMessage());
            }
        }
        return new ProjectOutcome(chapter, email, role, current.projectId(), current.status(), applied, null, null,
            null);
    }

    private OwnedProject execute(StartingLineStep step, OwnedProject current, RehearsalContext context,
                                 RosterKey owner, Role role, ProjectQuota quota, Long ownerId, Long superadminId) {
        return switch (step) {
            case CREATE_DRAFT -> OwnedProject.newDraft(createDraftProjectUseCase.create(CreateDraftProjectCommand.builder()
                .gisuId(context.gisuId()).productOwnerMemberId(ownerId).requesterMemberId(ownerId).build()), ownerId);
            case UPDATE_INFO -> {
                updateProjectUseCase.update(UpdateProjectCommand.builder()
                    .projectId(current.projectId()).requesterMemberId(ownerId)
                    .name("%s %s의 프로젝트".formatted(role == Role.QA ? "[QA]" : "[SEED]", context.displayName(owner)))
                    .description("매칭 리허설용 프로젝트입니다.")
                    .build());
                yield current.withName();
            }
            case UPSERT_FORM -> {
                upsertProjectApplicationFormUseCase.upsert(UpsertApplicationFormCommand.builder()
                    .projectId(current.projectId()).requesterMemberId(ownerId)
                    .description("매칭 리허설용 지원서")
                    .sections(dummyApplicationFormFactory.defaultSections())
                    .build());
                yield current.withForm();
            }
            case SUBMIT -> current.withStatus(submitProjectUseCase.submit(SubmitProjectCommand.builder()
                .projectId(current.projectId()).requesterMemberId(ownerId).build()));
            case SET_QUOTAS -> {
                updatePartQuotasUseCase.update(UpdatePartQuotasCommand.builder()
                    .projectId(current.projectId()).requesterMemberId(superadminId)
                    .entries(quota.entries().entrySet().stream()
                        .map(entry -> UpdatePartQuotasCommand.Entry.builder()
                            .part(entry.getKey()).quota((long) entry.getValue()).build())
                        .toList())
                    .build());
                yield current.withQuotas(quota.entries());
            }
            case PUBLISH -> current.withStatus(publishProjectUseCase.publish(PublishProjectCommand.builder()
                .projectId(current.projectId()).requesterMemberId(superadminId).build()));
        };
    }
}
