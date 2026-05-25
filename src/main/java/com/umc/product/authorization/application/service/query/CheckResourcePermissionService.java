package com.umc.product.authorization.application.service.query;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionQuery;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
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
        return hasPermission(memberId, resourceType, resourceId, (PermissionType) null);
    }

    @Override
    public ResourcePermissionInfo hasPermission(
        Long memberId,
        ResourceType resourceType,
        Long resourceId,
        PermissionType permissionType
    ) {
        validateResourceTypeSupported(resourceType);
        if (permissionType != null) {
            validatePermissionSupported(resourceType, permissionType);
        }

        SubjectAttributes subjectAttributes = checkPermissionUseCase.loadSubject(memberId);
        return hasPermission(subjectAttributes, resourceType, resourceId, permissionType);
    }

    private ResourcePermissionInfo hasPermission(
        SubjectAttributes subjectAttributes,
        ResourceType resourceType,
        Long resourceId,
        PermissionType permissionType
    ) {
        if (permissionType != null) {
            return hasPermission(subjectAttributes, resourceType, resourceId, List.of(permissionType));
        }

        return hasPermission(subjectAttributes, resourceType, resourceId, getSortedSupportedPermissions(resourceType));
    }

    @Override
    public List<ResourcePermissionInfo> batchHasPermission(Long memberId, List<ResourcePermissionQuery> queries) {
        List<ResourcePermissionQuery> validQueries = validateBatchQueries(queries);
        SubjectAttributes subjectAttributes = checkPermissionUseCase.loadSubject(memberId);

        List<ResourcePermissionInfo> results = new ArrayList<>();
        for (ResourcePermissionQuery query : validQueries) {
            List<Long> resourceIds = query.resourceIds();
            if (resourceIds == null) {
                results.add(hasPermission(subjectAttributes, query.resourceType(), null, query.permissionTypes()));
                continue;
            }

            for (Long resourceId : resourceIds) {
                results.add(hasPermission(subjectAttributes, query.resourceType(), resourceId, query.permissionTypes()));
            }
        }

        return results;
    }

    private ResourcePermissionInfo hasPermission(
        SubjectAttributes subjectAttributes,
        ResourceType resourceType,
        Long resourceId,
        List<PermissionType> permissionTypes
    ) {
        if (permissionTypes == null) {
            return hasPermission(subjectAttributes, resourceType, resourceId, getSortedSupportedPermissions(resourceType));
        }

        Map<PermissionType, Boolean> permissions = new LinkedHashMap<>();
        evaluateAndPutPermissions(subjectAttributes, resourceType, resourceId, permissionTypes, permissions);

        return new ResourcePermissionInfo(resourceType, resourceId, permissions);
    }

    private List<ResourcePermissionQuery> validateBatchQueries(List<ResourcePermissionQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "권한 배치 조회 요청은 최소 1개 이상의 query를 포함해야 합니다."
            );
        }

        for (ResourcePermissionQuery query : queries) {
            validateBatchQuery(query);
        }

        return queries;
    }

    private void validateBatchQuery(ResourcePermissionQuery query) {
        if (query == null) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "권한 배치 조회 query는 null일 수 없습니다."
            );
        }

        validateResourceTypeSupported(query.resourceType());
        validateResourceIds(query.resourceIds());
        validatePermissionTypes(query.resourceType(), query.permissionTypes());
    }

    private void validateResourceTypeSupported(ResourceType resourceType) {
        if (resourceType == null) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "resourceType은 필수입니다."
            );
        }

        if (!supportedResourceTypes.contains(resourceType)) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.NO_EVALUATOR_MATCHING_RESOURCE_TYPE,
                "리소스 타입 '" + resourceType + "'에 대한 권한 조회는 아직 지원되지 않습니다."
            );
        }
    }

    private void validateResourceIds(List<Long> resourceIds) {
        if (resourceIds == null) {
            return;
        }

        if (resourceIds.isEmpty() || new ArrayList<>(resourceIds).contains(null)) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "resourceIds는 비어 있거나 null 값을 포함할 수 없습니다."
            );
        }
    }

    private void validatePermissionTypes(ResourceType resourceType, List<PermissionType> permissionTypes) {
        if (permissionTypes == null) {
            return;
        }

        if (permissionTypes.isEmpty() || new ArrayList<>(permissionTypes).contains(null)) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                "permissionTypes는 비어 있거나 null 값을 포함할 수 없습니다."
            );
        }

        permissionTypes.forEach(permissionType -> validatePermissionSupported(resourceType, permissionType));
    }

    private void validatePermissionSupported(ResourceType resourceType, PermissionType permissionType) {
        if (!resourceType.supports(permissionType)) {
            throw new AuthorizationDomainException(
                AuthorizationErrorCode.INVALID_INPUT_VALUE,
                String.format("리소스 타입 '%s'은(는) '%s' 권한을 지원하지 않습니다.", resourceType, permissionType)
            );
        }
    }

    private List<PermissionType> getSortedSupportedPermissions(ResourceType resourceType) {
        return resourceType.getSupportedPermissions().stream()
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toList();
    }

    private void evaluateAndPutPermissions(
        SubjectAttributes subjectAttributes,
        ResourceType resourceType,
        Long resourceId,
        List<PermissionType> permissionTypes,
        Map<PermissionType, Boolean> permissions
    ) {
        permissionTypes.stream()
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .forEach(permissionType ->
                putPermissionResult(subjectAttributes, resourceType, resourceId, permissionType, permissions)
            );
    }

    private void putPermissionResult(
        SubjectAttributes subjectAttributes,
        ResourceType resourceType,
        Long resourceId,
        PermissionType permissionType,
        Map<PermissionType, Boolean> permissions
    ) {
        ResourcePermission permission = resourceId != null
            ? ResourcePermission.of(resourceType, resourceId, permissionType)
            : ResourcePermission.ofType(resourceType, permissionType);

        boolean hasAccess = checkPermissionUseCase.check(subjectAttributes, permission);
        permissions.put(permissionType, hasAccess);
    }
}
