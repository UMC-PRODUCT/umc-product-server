package com.umc.product.authorization.adapter.in.web.dto.response;

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
}
