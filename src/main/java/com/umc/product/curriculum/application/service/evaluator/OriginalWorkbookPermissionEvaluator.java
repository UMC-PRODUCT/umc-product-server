package com.umc.product.curriculum.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import org.springframework.stereotype.Component;

@Component
public class OriginalWorkbookPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ORIGINAL_WORKBOOK;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes,
                            ResourcePermission resourcePermission) {
        if (resourcePermission.permission() == PermissionType.RELEASE) {
            // 학교 운영진(회장, 부회장, 파트장, 기타 운영진)만 READ 권한
            return subjectAttributes.roleAttributes().stream()
                .anyMatch(role -> role.roleType().isCentralMember());
        }

        throw new CommonException(CommonErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED);
    }
}
