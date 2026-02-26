package com.umc.product.authorization.adapter.in.web.dto.response;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import java.util.List;

public record ResourcePermissionResponse(
    ResourceType resourceType,
    Long resourceId,
    List<PermissionInfo> permissions
) {
    public record PermissionInfo(
        PermissionType permissionType,
        boolean hasPermission
    ) {
    }

    public static ResourcePermissionResponse from(ResourcePermissionInfo info) {
        List<PermissionInfo> permissionInfos = info.permissions().entrySet().stream()
            .map(entry -> new PermissionInfo(entry.getKey(), entry.getValue()))
            .toList();

        return new ResourcePermissionResponse(info.resourceType(), info.resourceId(), permissionInfos);
    }
}
