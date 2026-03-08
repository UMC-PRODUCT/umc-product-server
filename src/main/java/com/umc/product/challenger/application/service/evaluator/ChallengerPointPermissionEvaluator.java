package com.umc.product.challenger.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ChallengerPointPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.CHALLENGER_POINT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case WRITE -> canCreate(subjectAttributes);
            case EDIT -> canUpdate(subjectAttributes, resourcePermission);
            case DELETE -> canDelete(subjectAttributes);
            default -> throw new CommonException(CommonErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED); // 지원하지 않는 권한 유형은 거부
        };
    }

    private boolean canCreate(SubjectAttributes subjectAttributes) {
        // 교내 회장/부회장 이상만 가능함
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(roleAttribute -> roleAttribute.roleType().isSchoolCore());
    }

    private boolean canUpdate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        // 교내 회장/부회장 이상만 가능함
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(roleAttribute -> roleAttribute.roleType().isSchoolCore());
    }

    private boolean canDelete(SubjectAttributes subjectAttributes) {
        // 중앙운영사무국 총괄단만 가능함
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(roleAttribute -> roleAttribute.roleType().isCentralCore());
    }
}
