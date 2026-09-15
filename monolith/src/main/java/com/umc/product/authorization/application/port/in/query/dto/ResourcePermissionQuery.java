package com.umc.product.authorization.application.port.in.query.dto;

import java.util.List;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;

public record ResourcePermissionQuery(
    ResourceType resourceType,
    List<Long> resourceIds,
    List<PermissionType> permissionTypes
) {

    public static ResourcePermissionQuery of(
        ResourceType resourceType,
        List<Long> resourceIds,
        List<PermissionType> permissionTypes
    ) {
        return new ResourcePermissionQuery(resourceType, resourceIds, permissionTypes);
    }
}
