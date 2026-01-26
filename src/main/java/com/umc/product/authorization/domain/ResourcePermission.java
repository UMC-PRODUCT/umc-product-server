package com.umc.product.authorization.domain;

import static java.util.Objects.requireNonNull;

/**
 * 리소스에 대한 권한 정보
 */
public record ResourcePermission(
        ResourceType resourceType,  // CURRICULUM, SCHEDULE, NOTICE 등
        String resourceId,          // "123" (optional, null이면 타입 전체)
        PermissionType permission
) {
    public ResourcePermission {
        requireNonNull(resourceType, "리소스 유형은 null일 수 없습니다.");
        requireNonNull(permission, "권한 유형은 null일 수 없습니다.");
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
}
