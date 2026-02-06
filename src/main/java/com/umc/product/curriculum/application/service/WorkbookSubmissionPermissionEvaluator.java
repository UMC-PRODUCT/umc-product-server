package com.umc.product.curriculum.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import org.springframework.stereotype.Component;

/**
 * WorkbookSubmission(워크북 제출 현황) 리소스에 대한 권한 평가
 */
@Component
public class WorkbookSubmissionPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.WORKBOOK_SUBMISSION;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes,
                            ResourcePermission resourcePermission) {
        if (resourcePermission.permission() == PermissionType.READ) {
            // 학교 운영진(회장, 부회장, 파트장, 기타 운영진)만 READ 권한
            return subjectAttributes.roleAttributes().stream()
                .anyMatch(role -> role.roleType().isSchoolAdmin());
        }

        // 지원하지 않는 권한 타입
        throw new AuthorizationDomainException(
            AuthorizationErrorCode.INVALID_RESOURCE_PERMISSION_GIVEN,
            "WorkbookSubmissionPermissionEvaluator에서 지원하지 않는 권한 유형에 대한 평가가 시도되었습니다: " + resourcePermission.permission());
    }
}
