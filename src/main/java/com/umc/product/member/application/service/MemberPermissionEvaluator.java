package com.umc.product.member.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.MEMBER;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> canReadMember(subjectAttributes, resourcePermission);
            case DELETE -> canDeleteMember(subjectAttributes);
            case MANAGE -> subjectAttributes.isSystemAdmin();
            default ->
                throw new CommonException(CommonErrorCode.INTERNAL_SERVER_ERROR, "PE 관련 에러가 발생하였습니다. 관리자에게 문의하세요.");
        };
    }

    // === PRIVATE METHODS ===

    private boolean canReadMember(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return subjectAttributes.isSystemAdmin() || !subjectAttributes.gisuChallengerInfos().isEmpty();
    }

    private boolean canDeleteMember(SubjectAttributes subjectAttributes) {
        return subjectAttributes.isSystemAdmin()
            || subjectAttributes.roleAttributes().stream()
                .anyMatch(roleAttribute -> roleAttribute.roleType().isAtLeastCentralCore());
    }
}
