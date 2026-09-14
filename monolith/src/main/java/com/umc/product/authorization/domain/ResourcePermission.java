package com.umc.product.authorization.domain;

import static java.util.Objects.requireNonNull;

import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;

/**
 * 리소스에 대한 권한 정보
 */
public record ResourcePermission(
    ResourceType resourceType,  // CURRICULUM, SCHEDULE, NOTICE 등
    // "123" (optional, null이면 타입 전체)
    // 호환성을 위해서 String으로 설정, Long인 ID 값은 메소드 활용
    String resourceId,
    PermissionType permission
) {
    public ResourcePermission {
        requireNonNull(resourceType, "리소스 유형은 null일 수 없습니다.");
        requireNonNull(permission, "권한 유형은 null일 수 없습니다.");

        // ResourceType이 해당 PermissionType을 지원하는지 검증
        resourceType.validatePermission(permission);
    }

    /**
     * 리소스 ID 없이 타입 전체에 대한 권한
     */
    public static ResourcePermission ofType(ResourceType resourceType, PermissionType permission) {
        return new ResourcePermission(resourceType, null, permission);
    }

    /**
     * 특정 리소스에 대한 권한
     */
    public static ResourcePermission of(ResourceType resourceType, String resourceId, PermissionType permission) {
        return new ResourcePermission(resourceType, resourceId, permission);
    }

    /**
     * 특정 리소스에 대한 권한 (Long ID)
     */
    public static ResourcePermission of(ResourceType resourceType, Long resourceId, PermissionType permission) {
        return new ResourcePermission(resourceType, resourceId.toString(), permission);
    }

    public Long getResourceIdAsLong() {
        if (resourceId == null) {
            return null;
        }

        try {
            return Long.valueOf(resourceId);
        } catch (Exception e) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_RESOURCE_ID_TYPE);
        }
    }
}
