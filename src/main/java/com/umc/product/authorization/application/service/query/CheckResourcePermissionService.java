package com.umc.product.authorization.application.service.query;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CheckResourcePermissionService implements ResourcePermissionUseCase {

    private final CheckPermissionUseCase checkPermissionUseCase;

    @Override
    public ResourcePermissionInfo hasPermission(Long memberId, ResourceType resourceType, Long resourceId) {

        Map<PermissionType, Boolean> permissions = new LinkedHashMap<>();

        for (PermissionType permissionType : resourceType.getSupportedPermissions()) {
            ResourcePermission permission = resourceId != null
                ? ResourcePermission.of(resourceType, resourceId, permissionType)
                : ResourcePermission.ofType(resourceType, permissionType);

            boolean hasAccess = checkPermissionUseCase.check(memberId, permission);
            permissions.put(permissionType, hasAccess);
        }

        return new ResourcePermissionInfo(resourceType, resourceId, permissions);
    }
}
