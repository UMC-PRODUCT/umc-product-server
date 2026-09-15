package com.umc.product.project.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo;

public record ProjectPermissionsResponse(
    List<ProjectPermissionResponse> projects
) {

    public static ProjectPermissionsResponse from(List<ProjectPermissionInfo> infos) {
        return new ProjectPermissionsResponse(infos.stream()
            .map(ProjectPermissionResponse::from)
            .toList());
    }

    public record ProjectPermissionResponse(
        Long projectId,
        boolean exists,
        ProjectPermissionCapabilityInfo canEditInfo,
        ProjectPermissionCapabilityInfo canTransferOwnership,
        ProjectPermissionCapabilityInfo canDelete,
        ApplicationFormPermissionsResponse applicationForm,
        PartQuotaPermissionsResponse partQuota,
        StatusPermissionsResponse status,
        ApplicationPermissionsResponse application,
        MemberPermissionsResponse member,
        StatisticsPermissionsResponse statistics
    ) {

        private static ProjectPermissionResponse from(ProjectPermissionInfo info) {
            return new ProjectPermissionResponse(
                info.projectId(),
                info.exists(),
                info.canEditInfo(),
                info.canTransferOwnership(),
                info.canDelete(),
                ApplicationFormPermissionsResponse.from(info.applicationForm()),
                PartQuotaPermissionsResponse.from(info.partQuota()),
                StatusPermissionsResponse.from(info.status()),
                ApplicationPermissionsResponse.from(info.application()),
                MemberPermissionsResponse.from(info.member()),
                StatisticsPermissionsResponse.from(info.statistics())
            );
        }
    }

    public record ApplicationFormPermissionsResponse(
        ProjectPermissionCapabilityInfo canRead,
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canEdit,
        ProjectPermissionCapabilityInfo canPublish,
        ProjectPermissionCapabilityInfo canDelete
    ) {

        private static ApplicationFormPermissionsResponse from(
            ProjectPermissionInfo.ApplicationFormPermissions permissions
        ) {
            return new ApplicationFormPermissionsResponse(
                permissions.canRead(),
                permissions.canCreate(),
                permissions.canEdit(),
                permissions.canPublish(),
                permissions.canDelete()
            );
        }
    }

    public record PartQuotaPermissionsResponse(
        ProjectPermissionCapabilityInfo canEdit
    ) {

        private static PartQuotaPermissionsResponse from(ProjectPermissionInfo.PartQuotaPermissions permissions) {
            return new PartQuotaPermissionsResponse(permissions.canEdit());
        }
    }

    public record StatusPermissionsResponse(
        ProjectPermissionCapabilityInfo canRequestReview,
        ProjectPermissionCapabilityInfo canPublish,
        ProjectPermissionCapabilityInfo canComplete,
        ProjectPermissionCapabilityInfo canAbort
    ) {

        private static StatusPermissionsResponse from(ProjectPermissionInfo.StatusPermissions permissions) {
            return new StatusPermissionsResponse(
                permissions.canRequestReview(),
                permissions.canPublish(),
                permissions.canComplete(),
                permissions.canAbort()
            );
        }
    }

    public record ApplicationPermissionsResponse(
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canReadList,
        ProjectPermissionCapabilityInfo canDecide
    ) {

        private static ApplicationPermissionsResponse from(ProjectPermissionInfo.ApplicationPermissions permissions) {
            return new ApplicationPermissionsResponse(
                permissions.canCreate(),
                permissions.canReadList(),
                permissions.canDecide()
            );
        }
    }

    public record MemberPermissionsResponse(
        ProjectPermissionCapabilityInfo canRead,
        ProjectPermissionCapabilityInfo canCreate,
        ProjectPermissionCapabilityInfo canDelete
    ) {

        private static MemberPermissionsResponse from(ProjectPermissionInfo.MemberPermissions permissions) {
            return new MemberPermissionsResponse(
                permissions.canRead(),
                permissions.canCreate(),
                permissions.canDelete()
            );
        }
    }

    public record StatisticsPermissionsResponse(
        ProjectPermissionCapabilityInfo canRead
    ) {

        private static StatisticsPermissionsResponse from(ProjectPermissionInfo.StatisticsPermissions permissions) {
            return new StatisticsPermissionsResponse(permissions.canRead());
        }
    }
}
