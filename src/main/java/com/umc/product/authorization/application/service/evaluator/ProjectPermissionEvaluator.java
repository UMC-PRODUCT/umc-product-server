package com.umc.product.authorization.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import org.springframework.stereotype.Component;

@Component
public class ProjectPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.PROJECT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        // TODO: 실제 로직 구현 필요
        // - READ: 인증된 사용자 전체 허용 (현재 true)
        // - WRITE: PM 챌린저(PLAN 파트)만 허용 (PROJECT-101)
        // - EDIT: DRAFT면 작성자 PM / PENDING_REVIEW·IN_PROGRESS면 Admin (PROJECT-102)
        // - DELETE / MANAGE: Admin(중앙운영사무국)만 (PROJECT-105, 108)
        if (resourcePermission.permission().equals(PermissionType.READ)) {
            return true;
        }

        // 현재는 skeleton. 임시로 중앙운영사무국 총괄만 통과.
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(roleAttribute -> roleAttribute.roleType().isAtLeastCentralCore());
    }
}
