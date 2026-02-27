package com.umc.product.authorization.application.service.query;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class CheckResourcePermissionService implements ResourcePermissionUseCase {

    private final CheckPermissionUseCase checkPermissionUseCase;
    private final Set<ResourceType> supportedResourceTypes;

    public CheckResourcePermissionService(
        CheckPermissionUseCase checkPermissionUseCase,
        List<ResourcePermissionEvaluator> evaluators) {
        this.checkPermissionUseCase = checkPermissionUseCase;
        this.supportedResourceTypes = evaluators.stream()
            .map(ResourcePermissionEvaluator::supportedResourceType)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public ResourcePermissionInfo hasPermission(Long memberId, ResourceType resourceType, Long resourceId) {
        if (!supportedResourceTypes.contains(resourceType)) {
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
