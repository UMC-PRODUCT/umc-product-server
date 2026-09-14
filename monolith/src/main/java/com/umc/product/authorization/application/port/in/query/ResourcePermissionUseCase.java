package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionQuery;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import java.util.List;

public interface ResourcePermissionUseCase {

    ResourcePermissionInfo hasPermission(Long memberId, ResourceType resourceType, Long resourceId);

    ResourcePermissionInfo hasPermission(
        Long memberId,
        ResourceType resourceType,
        Long resourceId,
        PermissionType permissionType
    );

    List<ResourcePermissionInfo> batchHasPermission(Long memberId, List<ResourcePermissionQuery> queries);

}
