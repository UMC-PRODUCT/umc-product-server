package com.umc.product.figma.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import org.springframework.stereotype.Component;

/**
 * Figma 통합 admin API 의 권한 평가기. ADR-007 의 결정에 따라 모든 권한(READ, MANAGE) 은 SUPER_ADMIN 만 통과한다. 그 외 역할 / 인증되지 않은 권한 enum 은 모두
 * 거부한다.
 */
@Component
public class FigmaPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.FIGMA;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ, MANAGE -> isSuperAdmin(subjectAttributes);
            default -> false;
        };
    }

    private boolean isSuperAdmin(SubjectAttributes subjectAttributes) {
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> role.roleType().isSuperAdmin());
    }
}
