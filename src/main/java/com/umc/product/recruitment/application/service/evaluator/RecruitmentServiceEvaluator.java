package com.umc.product.recruitment.application.service.evaluator;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentServiceEvaluator implements ResourcePermissionEvaluator {
    private final GetChallengerRoleUseCase roleUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.RECRUITMENT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subject,
                            ResourcePermission permission) {

        Long memberId = subject.memberId();

        boolean isCentral = roleUseCase.isCentralCore(memberId);
        boolean isSchoolAdmin = roleUseCase.isSchoolAdmin(memberId, subject.schoolId());

        switch (permission.permission()) {

            case READ:
                return true;

            case WRITE:
            case EDIT:
            case DELETE:
            case APPROVE:
                return isSchoolAdmin || isCentral;

            case MANAGE:
                return isCentral;

            default:
                return false;
        }
    }
}
