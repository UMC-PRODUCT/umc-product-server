package com.umc.product.organization.application.service.evaluator;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudyGroupPermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.STUDY_GROUP;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> getChallengerRoleUseCase.isSchoolAdmin(
                subjectAttributes.memberId(), subjectAttributes.schoolId());
            case WRITE, EDIT, DELETE -> getChallengerRoleUseCase.isSchoolCore(
                subjectAttributes.memberId(), subjectAttributes.schoolId());
            default -> throw new AuthorizationDomainException(AuthorizationErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED,
                "StudyGroupPermissionEvaluator에서 해당 PermissionType을 지원하지 않습니다: " + resourcePermission.permission());
        };
    }
}
