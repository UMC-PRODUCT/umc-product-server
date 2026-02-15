package com.umc.product.authorization.application.service.query;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CheckResourcePermissionService implements ResourcePermissionUseCase {

    // 공지, 워크북 제출 외 다른 부분에 대한 evaluator가 구현되어있지 않으므로 현재는 이 두 리소스 타입에 대해서만 권한 조회를 지원함
    // TODO: 향후 evaluator가 구현되는 리소스 타입이 늘어날 때마다 이 부분도 함께 업데이트 필요, 전부 구현되면 제거
    private static final Set<ResourceType> SUPPORTED_RESOURCE_TYPES = Set.of(
        ResourceType.NOTICE,
        ResourceType.WORKBOOK_SUBMISSION,
        ResourceType.SCHEDULE,
        ResourceType.ATTENDANCE_SHEET,
        ResourceType.ATTENDANCE_RECORD
    );

    private final CheckPermissionUseCase checkPermissionUseCase;

    @Override
    public ResourcePermissionInfo hasPermission(Long memberId, ResourceType resourceType, Long resourceId) {
        if (!SUPPORTED_RESOURCE_TYPES.contains(resourceType)) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.NO_EVALUATOR_MATCHING_RESOURCE_TYPE,
                "리소스 타입 '" + resourceType + "'에 대한 권한 조회는 아직 지원되지 않습니다."
            );
        }

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
