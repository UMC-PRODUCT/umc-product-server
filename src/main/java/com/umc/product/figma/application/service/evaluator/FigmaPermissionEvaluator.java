package com.umc.product.figma.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import org.springframework.stereotype.Component;

/**
 * Figma 통합 admin API 의 권한 평가기. 모든 권한(READ, MANAGE) 은 전역 관리자만 통과한다.
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
            case READ, MANAGE -> subjectAttributes.isSystemAdmin();
            default -> false;
        };
    }
}
