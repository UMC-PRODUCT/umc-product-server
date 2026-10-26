package com.umc.product.project.application.service.query;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.project.application.access.ProjectApplicationAccessScope;
import com.umc.product.project.application.access.ProjectApplicationAccessScopeResolver;
import com.umc.product.project.application.access.ProjectApplicationDecisionPolicy;
import com.umc.product.project.application.port.in.query.GetProjectPermissionsUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.ApplicationFormPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.ApplicationPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.MemberPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.PartQuotaPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.StatisticsPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.StatusPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectStatisticsAccessPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectPermissionQueryService implements GetProjectPermissionsUseCase {

    private static final ProjectPermissionCapabilityInfo NOT_IMPLEMENTED_FORM_PUBLISH =
        ProjectPermissionCapabilityInfo.denied(
            ProjectPermissionReason.NOT_IMPLEMENTED,
            "아직은 지원 폼 공개를 별도로 지원하지 않아요."
        );
    private static final ProjectPermissionCapabilityInfo NOT_IMPLEMENTED_FORM_DELETE =
        ProjectPermissionCapabilityInfo.denied(
            ProjectPermissionReason.NOT_IMPLEMENTED,
            "아직은 지원 폼 삭제를 별도로 지원하지 않아요."
        );

    private final Clock clock;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ProjectApplicationAccessScopeResolver projectApplicationAccessScopeResolver;
    private final ProjectStatisticsAccessPolicy projectStatisticsAccessPolicy;
    private final ProjectMatchingChapterResolver projectMatchingChapterResolver;
    private final ProjectMatchingAdjustmentPolicy projectMatchingAdjustmentPolicy;

    @Override
    public List<ProjectPermissionInfo> listByProjectIds(Long requesterMemberId, List<Long> projectIds) {
        List<Long> uniqueIds = deduplicate(projectIds);
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        SubjectAttributes subject = checkPermissionUseCase.loadSubject(requesterMemberId);
        Map<Long, Project> projectsById = loadProjectPort.listByIds(uniqueIds).stream()
            .collect(Collectors.toMap(
                Project::getId,
                project -> project,
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        Map<Long, ProjectApplicationForm> formsByProjectId =
            loadProjectApplicationFormPort.findAllByProjectIds(uniqueIds);
        Map<Long, List<ProjectPartQuota>> quotasByProjectId =
            loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(uniqueIds);
        PermissionCache permissionCache = new PermissionCache(subject);
        Instant now = clock.instant();

        return uniqueIds.stream()
            .map(projectId -> {
                Project project = projectsById.get(projectId);
                if (project == null) {
                    return ProjectPermissionInfo.notFound(projectId);
                }
                ProjectCapabilityContext context = ProjectCapabilityContext.of(
                    project,
                    formsByProjectId.get(projectId),
                    quotasByProjectId.getOrDefault(projectId, List.of()),
                    permissionCache.openMatchingRounds(project.getChapterId(), now),
                    permissionCache
                );
                return buildInfo(requesterMemberId, subject, context, now);
            })
            .toList();
    }

    private ProjectPermissionInfo buildInfo(
        Long requesterMemberId,
        SubjectAttributes subject,
        ProjectCapabilityContext context,
        Instant now
    ) {
        Project project = context.project();
        ProjectPermissionCapabilityInfo canEditInfo = requirePermission(
            context.projectPermission(PermissionType.EDIT),
            () -> context.hasOpenMatchingRound()
                ? denied(ProjectPermissionReason.PROJECT_UPDATE_DURING_APPLICATION_PERIOD)
                : ProjectPermissionCapabilityInfo.allow()
        );
        ProjectPermissionCapabilityInfo canTransferOwnership = requirePermission(
            context.projectPermission(PermissionType.EDIT),
            ProjectPermissionCapabilityInfo::allow
        );
        ProjectPermissionCapabilityInfo canDelete = canDeleteProject(context);

        return new ProjectPermissionInfo(
            project.getId(),
            true,
            canEditInfo,
            canTransferOwnership,
            canDelete,
            applicationFormPermissions(requesterMemberId, subject, context),
            partQuotaPermissions(context),
            statusPermissions(context),
            applicationPermissions(requesterMemberId, subject, context),
            memberPermissions(subject, context, now),
            statisticsPermissions(requesterMemberId, project)
        );
    }

    private ApplicationFormPermissions applicationFormPermissions(
        Long requesterMemberId,
        SubjectAttributes subject,
        ProjectCapabilityContext context
    ) {
        return new ApplicationFormPermissions(
            canReadApplicationForm(requesterMemberId, subject, context),
            canCreateApplicationForm(context),
            canEditApplicationForm(context),
            NOT_IMPLEMENTED_FORM_PUBLISH,
            NOT_IMPLEMENTED_FORM_DELETE
        );
    }

    private ProjectPermissionCapabilityInfo canReadApplicationForm(
        Long requesterMemberId,
        SubjectAttributes subject,
        ProjectCapabilityContext context
    ) {
        if (!context.projectPermission(PermissionType.READ)) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        if (!context.hasForm()) {
            return denied(ProjectPermissionReason.APPLICATION_FORM_NOT_FOUND);
        }
        if (!canReadApplicationFormPolicy(requesterMemberId, subject, context)) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        return ProjectPermissionCapabilityInfo.allow();
    }

    private ProjectPermissionCapabilityInfo canCreateApplicationForm(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.EDIT), () -> {
            if (context.hasForm()) {
                return ProjectPermissionCapabilityInfo.denied(
                    ProjectPermissionReason.NOT_IMPLEMENTED,
                    "아직은 프로젝트에 여러 개의 폼을 연결하는 것을 허용하지 않아요."
                );
            }
            return formEditable(context);
        });
    }

    private ProjectPermissionCapabilityInfo canEditApplicationForm(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.EDIT), () -> {
            if (!context.hasForm()) {
                return denied(ProjectPermissionReason.APPLICATION_FORM_NOT_FOUND);
            }
            return formEditable(context);
        });
    }

    private ProjectPermissionCapabilityInfo formEditable(ProjectCapabilityContext context) {
        ProjectStatus status = context.project().getStatus();
        if (status == ProjectStatus.DRAFT || status == ProjectStatus.PENDING_REVIEW) {
            return ProjectPermissionCapabilityInfo.allow();
        }
        if (status == ProjectStatus.IN_PROGRESS) {
            if (context.hasOpenMatchingRound()) {
                return denied(ProjectPermissionReason.ACTIVE_MATCHING_ROUND_EXISTS);
            }
            return ProjectPermissionCapabilityInfo.allow();
        }
        return denied(ProjectPermissionReason.INVALID_PROJECT_STATUS);
    }

    private PartQuotaPermissions partQuotaPermissions(ProjectCapabilityContext context) {
        return new PartQuotaPermissions(requirePermission(
            context.projectPermission(PermissionType.MANAGE),
            ProjectPermissionCapabilityInfo::allow
        ));
    }

    private StatusPermissions statusPermissions(ProjectCapabilityContext context) {
        return new StatusPermissions(
            canRequestReview(context),
            canPublishProject(context),
            canCompleteProject(context),
            canAbortProject(context)
        );
    }

    private ProjectPermissionCapabilityInfo canRequestReview(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.EDIT), () -> {
            Project project = context.project();
            if (project.getStatus() != ProjectStatus.DRAFT) {
                return denied(ProjectPermissionReason.INVALID_PROJECT_STATUS);
            }
            if (project.getName() == null || project.getName().isBlank()) {
                return denied(ProjectPermissionReason.PROJECT_INFO_REQUIRED);
            }
            if (!context.hasForm()) {
                return denied(ProjectPermissionReason.APPLICATION_FORM_NOT_FOUND);
            }
            return ProjectPermissionCapabilityInfo.allow();
        });
    }

    private ProjectPermissionCapabilityInfo canPublishProject(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.MANAGE), () -> {
            if (context.project().getStatus() != ProjectStatus.PENDING_REVIEW) {
                return denied(ProjectPermissionReason.INVALID_PROJECT_STATUS);
            }
            if (!context.hasForm()) {
                return denied(ProjectPermissionReason.APPLICATION_FORM_NOT_FOUND);
            }
            if (context.quotas().isEmpty()) {
                return denied(ProjectPermissionReason.PART_QUOTA_REQUIRED);
            }
            return ProjectPermissionCapabilityInfo.allow();
        });
    }

    private ProjectPermissionCapabilityInfo canAbortProject(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.MANAGE), () -> {
            if (context.project().getStatus() != ProjectStatus.IN_PROGRESS) {
                return ProjectPermissionCapabilityInfo.denied(
                    ProjectPermissionReason.INVALID_PROJECT_STATUS,
                    "현재 진행 중인 프로젝트만 중단 시킬 수 있어요."
                );
            }
            return ProjectPermissionCapabilityInfo.allow();
        });
    }

    private ProjectPermissionCapabilityInfo canCompleteProject(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.MANAGE), () -> {
            if (context.project().getStatus() != ProjectStatus.IN_PROGRESS) {
                return ProjectPermissionCapabilityInfo.denied(
                    ProjectPermissionReason.INVALID_PROJECT_STATUS,
                    "현재 진행 중인 프로젝트만 완료 처리할 수 있어요."
                );
            }
            return ProjectPermissionCapabilityInfo.allow();
        });
    }

    private ApplicationPermissions applicationPermissions(
        Long requesterMemberId,
        SubjectAttributes subject,
        ProjectCapabilityContext context
    ) {
        return new ApplicationPermissions(
            canCreateApplication(requesterMemberId, context),
            canReadApplicationList(requesterMemberId, context.project()),
            canDecideApplication(subject, context.project())
        );
    }

    private ProjectPermissionCapabilityInfo canCreateApplication(
        Long requesterMemberId,
        ProjectCapabilityContext context
    ) {
        if (!context.applicationWritePermission()) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        Project project = context.project();
        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            return denied(ProjectPermissionReason.INVALID_PROJECT_STATUS);
        }
        if (!context.hasForm()) {
            return denied(ProjectPermissionReason.APPLICATION_FORM_NOT_FOUND);
        }
        if (Objects.equals(project.getProductOwnerMemberId(), requesterMemberId)) {
            return denied(ProjectPermissionReason.PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED);
        }

        Optional<ChallengerInfo> challenger =
            context.challengerInfo(requesterMemberId);
        if (challenger.isEmpty()) {
            return denied(ProjectPermissionReason.NOT_PROJECT_GISU_CHALLENGER);
        }
        Optional<MatchingType> matchingType = MatchingType.fromPart(challenger.get().part());
        if (matchingType.isEmpty()) {
            return denied(ProjectPermissionReason.NOT_PROJECT_GISU_CHALLENGER);
        }
        if (context.quotas().stream().noneMatch(quota -> quota.getPart() == challenger.get().part())) {
            return denied(ProjectPermissionReason.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
        if (!context.isInProjectMatchingChapter(requesterMemberId)) {
            return denied(ProjectPermissionReason.CHAPTER_MISMATCH);
        }
        if (context.openMatchingRounds().stream().noneMatch(round -> round.getType() == matchingType.get())) {
            return denied(ProjectPermissionReason.MATCHING_ROUND_NOT_OPEN);
        }
        if (context.existsByGisuAndMember(requesterMemberId)) {
            return denied(ProjectPermissionReason.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM);
        }
        return ProjectPermissionCapabilityInfo.allow();
    }

    private ProjectPermissionCapabilityInfo canReadApplicationList(Long requesterMemberId, Project project) {
        ProjectApplicationAccessScope scope =
            projectApplicationAccessScopeResolver.resolveForProjectApplicantList(requesterMemberId, project);
        if (scope instanceof ProjectApplicationAccessScope.None) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        return ProjectPermissionCapabilityInfo.allow();
    }

    /**
     * 프론트용 "판정 가능" 플래그. 역할 조건은 판정 API 의 권한 평가기와 같은 {@link ProjectApplicationDecisionPolicy} 를 쓴다.
     * 판정 가능 시간은 차수마다 달라 프로젝트 단위 플래그에는 넣지 않고, 실제 판정 요청 시 도메인이 검증한다.
     */
    private ProjectPermissionCapabilityInfo canDecideApplication(SubjectAttributes subject, Project project) {
        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            return denied(ProjectPermissionReason.INVALID_PROJECT_STATUS);
        }
        if (!ProjectApplicationDecisionPolicy.canDecide(subject, project)) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        return ProjectPermissionCapabilityInfo.allow();
    }

    private MemberPermissions memberPermissions(SubjectAttributes subject, ProjectCapabilityContext context, Instant now) {
        ProjectPermissionCapabilityInfo read = requirePermission(
            context.projectPermission(PermissionType.READ),
            ProjectPermissionCapabilityInfo::allow
        );
        ProjectPermissionCapabilityInfo edit = requirePermission(
            context.projectPermission(PermissionType.EDIT),
            ProjectPermissionCapabilityInfo::allow
        );
        Project project = context.project();
        boolean staff = ProjectManualMatchingAccessPolicy.canManage(subject.toAuthoritySnapshot(), project);
        ProjectPermissionCapabilityInfo assign = requirePermission(staff && project.getStatus() == ProjectStatus.IN_PROGRESS,
            () -> manualMatchingPeriod(context, now));
        ProjectPermissionCapabilityInfo unassign = requirePermission(staff
            && project.getStatus() != ProjectStatus.COMPLETED && project.getStatus() != ProjectStatus.ABORTED,
            () -> manualMatchingPeriod(context, now));
        return new MemberPermissions(read, edit, edit, assign, unassign);
    }

    private ProjectPermissionCapabilityInfo manualMatchingPeriod(ProjectCapabilityContext context, Instant now) {
        return context.permissionCache().canChangeMembers(context.project().getChapterId(), now)
            ? ProjectPermissionCapabilityInfo.allow()
            : denied(ProjectPermissionReason.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
    }

    private StatisticsPermissions statisticsPermissions(Long requesterMemberId, Project project) {
        if (!projectStatisticsAccessPolicy.canReadProjectStatistics(requesterMemberId, project)) {
            return new StatisticsPermissions(ProjectPermissionCapabilityInfo.denied(
                ProjectPermissionReason.PERMISSION_DENIED,
                "통계를 조회할 권한이 없어요."
            ));
        }
        return new StatisticsPermissions(ProjectPermissionCapabilityInfo.allow());
    }

    private ProjectPermissionCapabilityInfo canDeleteProject(ProjectCapabilityContext context) {
        return requirePermission(context.projectPermission(PermissionType.DELETE), () -> {
            if (context.hasOpenMatchingRound()) {
                return ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.APPLICATION_PERIOD_IN_PROGRESS);
            }
            return ProjectPermissionCapabilityInfo.allow();
        });
    }

    private ProjectPermissionCapabilityInfo requirePermission(
        boolean hasPermission,
        Supplier<ProjectPermissionCapabilityInfo> allowedSupplier
    ) {
        if (!hasPermission) {
            return denied(ProjectPermissionReason.PERMISSION_DENIED);
        }
        return allowedSupplier.get();
    }

    private ProjectPermissionCapabilityInfo denied(ProjectPermissionReason reason) {
        return ProjectPermissionCapabilityInfo.denied(reason);
    }

    private boolean canReadApplicationFormPolicy(
        Long requesterMemberId,
        SubjectAttributes subject,
        ProjectCapabilityContext context
    ) {
        Project project = context.project();
        if (Objects.equals(requesterMemberId, project.getProductOwnerMemberId())) {
            return true;
        }
        if (isCentralCoreInGisu(subject, project.getGisuId())) {
            return true;
        }
        if (isChapterPresidentOf(subject, project.getChapterId(), project.getGisuId())) {
            return true;
        }
        return subject.gisuChallengerInfos().stream()
            .anyMatch(info -> Objects.equals(info.gisuId(), project.getGisuId()))
            || context.challengerInfo(requesterMemberId).isPresent();
    }

    private boolean isCentralCoreInGisu(SubjectAttributes subject, Long gisuId) {
        return subject.toAuthoritySnapshot().isCentralCoreInGisu(gisuId);
    }

    private boolean isChapterPresidentOf(SubjectAttributes subject, Long chapterId, Long gisuId) {
        return subject.roleAttributes().stream()
            .anyMatch(role -> role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                && Objects.equals(role.gisuId(), gisuId)
                && Objects.equals(role.organizationId(), chapterId));
    }

    private List<Long> deduplicate(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return projectIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(
                Collectors.toCollection(LinkedHashSet::new),
                set -> List.copyOf(set)
            ));
    }

    private class PermissionCache {

        private final SubjectAttributes subject;
        private final Map<Long, Map<PermissionType, Boolean>> projectPermissions = new LinkedHashMap<>();
        private final Map<Long, Boolean> applicationWritePermissions = new LinkedHashMap<>();
        private final Map<Long, List<ProjectMatchingRound>> openMatchingRounds = new LinkedHashMap<>();
        private final Map<Long, Boolean> manualMatchingAllowed = new LinkedHashMap<>();
        private final Map<Long, Optional<ChallengerInfo>> challengerInfos = new LinkedHashMap<>();
        private final Map<Long, Boolean> projectMemberExists = new LinkedHashMap<>();
        private final Map<Long, Optional<Long>> matchingChapterIds = new LinkedHashMap<>();

        private PermissionCache(SubjectAttributes subject) {
            this.subject = subject;
        }

        private boolean project(Long projectId, PermissionType permissionType) {
            return projectPermissions
                .computeIfAbsent(projectId, ignored -> new EnumMap<>(PermissionType.class))
                .computeIfAbsent(permissionType, type -> checkPermissionUseCase.check(
                    subject,
                    ResourcePermission.of(ResourceType.PROJECT, projectId, type)
                ));
        }

        private boolean applicationWrite(Long projectId) {
            return applicationWritePermissions.computeIfAbsent(projectId, id -> checkPermissionUseCase.check(
                subject,
                ResourcePermission.of(ResourceType.PROJECT_APPLICATION, id, PermissionType.WRITE)
            ));
        }

        private List<ProjectMatchingRound> openMatchingRounds(Long chapterId, Instant now) {
            return openMatchingRounds.computeIfAbsent(chapterId, id ->
                List.copyOf(loadProjectMatchingRoundPort.listOpenAt(id, now))
            );
        }

        // 같은 지부의 프로젝트들은 기간 판단 결과를 공유해 차수를 반복 조회하지 않는다.
        private boolean canChangeMembers(Long chapterId, Instant now) {
            return manualMatchingAllowed.computeIfAbsent(chapterId, id ->
                projectMatchingAdjustmentPolicy.isChangeAllowedInPeriod(loadProjectMatchingRoundPort.listByChapterId(id), now)
            );
        }

        private Optional<ChallengerInfo> challengerInfo(Long memberId, Long gisuId) {
            return challengerInfos.computeIfAbsent(gisuId, id ->
                getChallengerUseCase.findByMemberIdAndGisuId(memberId, id)
            );
        }

        private boolean existsByGisuAndMember(Long gisuId, Long memberId) {
            return projectMemberExists.computeIfAbsent(gisuId, id ->
                loadProjectMemberPort.existsByGisuAndMember(id, memberId)
            );
        }

        private Optional<Long> matchingChapterId(Long memberId, Long gisuId) {
            return matchingChapterIds.computeIfAbsent(gisuId, id ->
                projectMatchingChapterResolver.findMatchingChapterId(memberId, id)
            );
        }
    }

    private record ProjectCapabilityContext(
        Project project,
        ProjectApplicationForm form,
        List<ProjectPartQuota> quotas,
        List<ProjectMatchingRound> openMatchingRounds,
        PermissionCache permissionCache
    ) {

        private static ProjectCapabilityContext of(
            Project project,
            ProjectApplicationForm form,
            List<ProjectPartQuota> quotas,
            List<ProjectMatchingRound> openMatchingRounds,
            PermissionCache permissionCache
        ) {
            return new ProjectCapabilityContext(
                project,
                form,
                List.copyOf(quotas),
                List.copyOf(openMatchingRounds),
                permissionCache
            );
        }

        private boolean hasForm() {
            return form != null;
        }

        private boolean hasOpenMatchingRound() {
            return !openMatchingRounds.isEmpty();
        }

        private boolean projectPermission(PermissionType permissionType) {
            return permissionCache.project(project.getId(), permissionType);
        }

        private boolean applicationWritePermission() {
            return permissionCache.applicationWrite(project.getId());
        }

        private Optional<ChallengerInfo> challengerInfo(Long memberId) {
            return permissionCache.challengerInfo(memberId, project.getGisuId());
        }

        private boolean existsByGisuAndMember(Long memberId) {
            return permissionCache.existsByGisuAndMember(project.getGisuId(), memberId);
        }

        /**
         * 지원자의 매칭 지부가 프로젝트 지부와 같은지 확인합니다. 규칙은 {@code ProjectApplicationChapterPolicy} 와 같습니다.
         */
        private boolean isInProjectMatchingChapter(Long memberId) {
            return permissionCache.matchingChapterId(memberId, project.getGisuId())
                .map(chapterId -> Objects.equals(chapterId, project.getChapterId()))
                .orElse(false);
        }
    }
}
