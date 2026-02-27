package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.ResourceType;

public interface ResourcePermissionUseCase {

    ResourcePermissionInfo hasPermission(Long memberId, ResourceType resourceType, Long resourceId);

}
