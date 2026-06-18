package com.umc.product.project.application.port.in.query.dto;

public record ProjectPermissionInfo(
    Long projectId,
    boolean exists,
    ProjectPermissionCapabilityInfo canEditInfo,
    ProjectPermissionCapabilityInfo canTransferOwnership,
    ProjectPermissionCapabilityInfo canDelete,
    ApplicationFormPermissions applicationForm,
    PartQuotaPermissions partQuota,
    StatusPermissions status,
    ApplicationPermissions application,
    MemberPermissions member,
    StatisticsPermissions statistics
) {

    public static ProjectPermissionInfo notFound(Long projectId) {
        ProjectPermissionCapabilityInfo denied =
            ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.PROJECT_NOT_FOUND);
        return new ProjectPermissionInfo(
            projectId,
            false,
            denied,
            denied,
            denied,
            new ApplicationFormPermissions(denied, denied, denied, denied, denied),
            new PartQuotaPermissions(denied),
            new StatusPermissions(denied, denied, denied, denied),
            new ApplicationPermissions(denied, denied, denied),
            new MemberPermissions(denied, denied, denied),
            new StatisticsPermissions(denied)
        );
    }

    public record ApplicationFormPermissions(
        ProjectPermissionCapabilityInfo canRead,
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canEdit,
        ProjectPermissionCapabilityInfo canPublish,
        ProjectPermissionCapabilityInfo canDelete
    ) {
    }

    public record PartQuotaPermissions(
        ProjectPermissionCapabilityInfo canEdit
    ) {
    }

    public record StatusPermissions(
        ProjectPermissionCapabilityInfo canRequestReview,
        ProjectPermissionCapabilityInfo canPublish,
        ProjectPermissionCapabilityInfo canComplete,
        ProjectPermissionCapabilityInfo canAbort
    ) {
    }

    public record ApplicationPermissions(
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canReadList,
        ProjectPermissionCapabilityInfo canDecide
    ) {
    }

    public record MemberPermissions(
        ProjectPermissionCapabilityInfo canRead,
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canDelete
    ) {
    }

    public record StatisticsPermissions(
        ProjectPermissionCapabilityInfo canRead
    ) {
    }
}
