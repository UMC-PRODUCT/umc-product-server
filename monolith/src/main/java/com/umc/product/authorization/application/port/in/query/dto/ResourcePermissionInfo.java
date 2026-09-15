package com.umc.product.authorization.application.port.in.query.dto;

import java.util.Map;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;

public record ResourcePermissionInfo(
    ResourceType resourceType,
    Long resourceId,
    Map<PermissionType, Boolean> permissions
) {
}
