package com.umc.product.authorization.application.port.in.query.dto;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import java.util.Map;

public record ResourcePermissionInfo(
    ResourceType resourceType,
    Long resourceId,
    Map<PermissionType, Boolean> permissions
) {
}
