package com.umc.product.authorization.domain;

import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import java.util.Set;
import lombok.Getter;

/**
 * 권한 체크 대상이 되는 리소스 타입
 */
@Getter
public enum ResourceType {

    CURRICULUM("curriculum", "커리큘럼",
        Set.of(PermissionType.READ, PermissionType.WRITE, PermissionType.DELETE)),
    SCHEDULE("schedule", "일정",
        Set.of(PermissionType.READ, PermissionType.WRITE, PermissionType.DELETE)),
    NOTICE("notice", "공지사항",
        // WRITE는 Service 단에서 처리함
        Set.of(PermissionType.READ, PermissionType.DELETE)),
    CHAPTER("chapter", "지부",
        Set.of(PermissionType.WRITE, PermissionType.DELETE)),
    WORKBOOK_SUBMISSION("workbook_submission", "워크북 제출 현황",
        Set.of(PermissionType.READ)),
    ;

    private final String code;
    private final String description;
    private final Set<PermissionType> supportedPermissions;

    ResourceType(String code, String description, Set<PermissionType> supportedPermissions) {
        this.code = code;
        this.description = description;
        this.supportedPermissions = Set.copyOf(supportedPermissions); // immutable로 보호
    }

    /**
     * code로 ResourceType 찾기
     */
    public static ResourceType fromCode(String code) {
        for (ResourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown resource type: " + code);
    }

    /**
     * 해당 리소스 타입이 특정 권한을 지원하는지 확인
     */
    public boolean supports(PermissionType permission) {
        return supportedPermissions.contains(permission);
    }

    /**
     * 지원하지 않는 권한이면 예외를 발생시킴
     */
    public void validatePermission(PermissionType permission) {
        if (!supports(permission)) {
            String errorMessage =
                String.format("리소스 타입 '%s'은(는) '%s' 권한을 지원하지 않습니다. 지원 권한: %s",
                    this.name(), permission, supportedPermissions);

            throw new AuthorizationDomainException(
                AuthorizationErrorCode.PERMISSION_TYPE_NOT_SUPPORTED_BY_RESOURCE_TYPE,
                errorMessage);
        }
    }
}
