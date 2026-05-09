package com.umc.product.project.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.CreateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.service.policy.AutoDecisionResult;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingRoundCommandService implements
    CreateProjectMatchingRoundUseCase,
    UpdateProjectMatchingRoundUseCase,
    DeleteProjectMatchingRoundUseCase,
    AutoDecideProjectMatchingRoundUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final List<MatchingDecisionPolicy> matchingDecisionPolicies;
    private final Random matchingRandom;

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public Long create(CreateProjectMatchingRoundCommand command) {
        // 지부장 이상부터 매칭 차수에 대한 수정이 가능합니다.
        validateManageAccess(command.requesterMemberId(), command.chapterId());
        // 매칭 차수는 중복될 수 없습니다.
        validateNoOverlap(
            command.chapterId(), command.startsAt(), command.endsAt(), command.decisionDeadline());

        ProjectMatchingRound matchingRound = ProjectMatchingRound.create(
            command.name(),
            command.description(),
            command.type(),
            command.phase(),
            command.chapterId(),
            command.startsAt(),
            command.endsAt(),
            command.decisionDeadline()
        );

        return saveProjectMatchingRoundPort.save(matchingRound).getId();
    }

    @Override
    public void update(UpdateProjectMatchingRoundCommand command) {
        ProjectMatchingRound matchingRound = loadProjectMatchingRoundPort.getById(command.matchingRoundId());

        String name = command.name() != null ? command.name() : matchingRound.getName();
        String description = command.description() != null ? command.description() : matchingRound.getDescription();
        MatchingType type = command.type() != null ? command.type() : matchingRound.getType();
        MatchingPhase phase = command.phase() != null ? command.phase() : matchingRound.getPhase();
        Instant startsAt = command.startsAt() != null ? command.startsAt() : matchingRound.getStartsAt();
        Instant endsAt = command.endsAt() != null ? command.endsAt() : matchingRound.getEndsAt();
        Instant decisionDeadline = command.decisionDeadline() != null
            ? command.decisionDeadline()
            : matchingRound.getDecisionDeadline();

        validateManageAccess(command.requesterMemberId(), matchingRound.getChapterId());
        validateNoOverlapExceptId(
            matchingRound.getId(),
            matchingRound.getChapterId(),
            startsAt,
            endsAt,
            decisionDeadline
        );

        matchingRound.update(
            name,
            description,
            type,
            phase,
            startsAt,
            endsAt,
            decisionDeadline
        );
    }

    @Override
    public void delete(Long matchingRoundId, Long requesterMemberId) {
        ProjectMatchingRound matchingRound = loadProjectMatchingRoundPort.getById(matchingRoundId);
        validateManageAccess(requesterMemberId, matchingRound.getChapterId());

        if (loadProjectApplicationPort.existsByAppliedMatchingRoundId(matchingRoundId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_DELETE_CONFLICT);
        }

        saveProjectMatchingRoundPort.delete(matchingRound);
    }

    @Override
    public void autoDecide(Long matchingRoundId, Long executedByMemberId) {
        ProjectMatchingRound round = loadProjectMatchingRoundPort.getById(matchingRoundId);

        if (round.getAutoDecisionExecutedAt() != null) {
            return;
        }
        if (!round.isDecisionDeadlinePassed(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
        }

        MatchingDecisionPolicy policy = resolvePolicy(round.getType());

        List<ProjectApplication> applicants = loadProjectApplicationPort
            .listByMatchingRoundId(matchingRoundId).stream()
            .filter(a -> isDecidableStatus(a.getStatus()))
            .toList();

        if (applicants.isEmpty()) {
            round.executeAutoDecision(executedByMemberId);
            return;
        }

        Map<Long, ChallengerPart> applicantPart = resolveApplicantParts(applicants);
        Map<ProjectPartKey, List<ProjectApplication>> grouped = groupByProjectAndPart(applicants, applicantPart);

        Set<Long> approvedIds = new HashSet<>();
        Set<Long> rejectedIds = new HashSet<>();
        for (Map.Entry<ProjectPartKey, List<ProjectApplication>> entry : grouped.entrySet()) {
            int quota = findQuota(entry.getKey().projectId(), entry.getKey().part());
            AutoDecisionResult result = policy.decideAutomatically(entry.getValue(), quota, matchingRandom);
            approvedIds.addAll(result.approvedIds());
            rejectedIds.addAll(result.rejectedIds());
        }

        applyDecisions(applicants, approvedIds, rejectedIds, executedByMemberId);
        saveProjectApplicationPort.saveAll(applicants);

        registerApprovedMembers(applicants, approvedIds, applicantPart, executedByMemberId);

        round.executeAutoDecision(executedByMemberId);
    }

    private MatchingDecisionPolicy resolvePolicy(MatchingType type) {
        return matchingDecisionPolicies.stream()
            .filter(p -> p.supportedType() == type)
            .findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND));
    }

    private boolean isDecidableStatus(ProjectApplicationStatus status) {
        return status == ProjectApplicationStatus.SUBMITTED
            || status == ProjectApplicationStatus.APPROVED
            || status == ProjectApplicationStatus.REJECTED;
    }

    private Map<Long, ChallengerPart> resolveApplicantParts(List<ProjectApplication> applicants) {
        Long gisuId = applicants.get(0).getApplicationForm().getProject().getGisuId();
        return applicants.stream().collect(Collectors.toMap(
            ProjectApplication::getId,
            a -> getChallengerUseCase.getByMemberIdAndGisuId(a.getApplicantMemberId(), gisuId).part()
        ));
    }

    private Map<ProjectPartKey, List<ProjectApplication>> groupByProjectAndPart(
        List<ProjectApplication> applicants, Map<Long, ChallengerPart> applicantPart
    ) {
        return applicants.stream().collect(Collectors.groupingBy(
            a -> new ProjectPartKey(
                a.getApplicationForm().getProject().getId(),
                applicantPart.get(a.getId())
            )
        ));
    }

    private int findQuota(Long projectId, ChallengerPart part) {
        return loadProjectPartQuotaPort.listByProjectId(projectId).stream()
            .filter(q -> q.getPart() == part)
            .findFirst()
            .map(q -> q.getQuota().intValue())
            .orElse(0);
    }

    private void applyDecisions(
        List<ProjectApplication> applicants,
        Set<Long> approvedIds,
        Set<Long> rejectedIds,
        Long executedByMemberId
    ) {
        for (ProjectApplication app : applicants) {
            ProjectApplicationStatus target = approvedIds.contains(app.getId())
                ? ProjectApplicationStatus.APPROVED
                : ProjectApplicationStatus.REJECTED;
            if (app.getStatus() != target) {
                app.applyAutoDecision(target, executedByMemberId);
            }
        }
    }

    private void registerApprovedMembers(
        List<ProjectApplication> applicants,
        Set<Long> approvedIds,
        Map<Long, ChallengerPart> applicantPart,
        Long executedByMemberId
    ) {
        for (ProjectApplication app : applicants) {
            if (!approvedIds.contains(app.getId())) {
                continue;
            }
            Project project = app.getApplicationForm().getProject();
            ChallengerPart part = applicantPart.get(app.getId());
            ProjectMember member = ProjectMember.create(
                project, app.getApplicantMemberId(), part, executedByMemberId
            );
            saveProjectMemberPort.save(member);
        }
    }

    private record ProjectPartKey(Long projectId, ChallengerPart part) {}

    private void validateNoOverlap(
        Long chapterId, Instant startsAt, Instant endsAt, Instant decisionDeadline
    ) {
        ProjectMatchingRound.validateDates(startsAt, endsAt, decisionDeadline);
        if (!loadProjectMatchingRoundPort.listOverlapping(chapterId, startsAt, decisionDeadline).isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED);
        }
    }

    private void validateNoOverlapExceptId(
        Long id, Long chapterId, Instant startsAt, Instant endsAt, Instant decisionDeadline
    ) {
        ProjectMatchingRound.validateDates(startsAt, endsAt, decisionDeadline);
        if (!loadProjectMatchingRoundPort.listOverlappingExceptId(
            id, chapterId, startsAt, decisionDeadline).isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED);
        }
    }

    private void validateManageAccess(Long memberId, Long chapterId) {
        List<ChallengerRoleInfo> roles = getChallengerRoleUseCase.findAllByMemberId(memberId);
        boolean allowed = roles.stream()
            .anyMatch(role -> role.roleType().isAtLeastCentralCore()
                || (role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                && Objects.equals(role.organizationId(), chapterId)));

        if (!allowed) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }
    }
}
