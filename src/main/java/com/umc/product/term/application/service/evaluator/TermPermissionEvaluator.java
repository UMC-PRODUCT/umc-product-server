package com.umc.product.term.application.service.evaluator;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TermPermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    /**
     * 이 Evaluator가 처리할 수 있는 ResourceType
     */
    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.TERM;
    }

    /**
     * 특정 리소스에 대한 권한 평가
     */
    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        PermissionType permissionType = resourcePermission.permission();

        return switch (permissionType) {
            case WRITE -> canWriteTerm(subjectAttributes);
            default ->
                throw new TermDomainException(TermErrorCode.TERM_PERMISSION_DENIED, "TermPE - 지원하지 않는 권한 유형입니다.");
        };
    }

    private boolean canWriteTerm(SubjectAttributes subjectAttributes) {
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(roles -> roles.roleType().isSuperAdmin());
    }
}
