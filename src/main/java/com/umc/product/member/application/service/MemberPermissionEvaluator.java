package com.umc.product.member.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberPermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.MEMBER;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> canReadMember(subjectAttributes, resourcePermission);
            case DELETE -> canDeleteMember(subjectAttributes);
            default ->
                throw new CommonException(CommonErrorCode.INTERNAL_SERVER_ERROR, "PE 관련 에러가 발생하였습니다. 관리자에게 문의하세요.");
        };
    }

    // === PRIVATE METHODS ===

    private boolean canReadMember(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (isSelf(subjectAttributes, resourcePermission)) {
            return true;
        }

        MemberInfo targetMemberInfo = getMemberUseCase.getMemberInfoById(resourcePermission.getResourceIdAsLong());

        return isSchoolCoreOf(subjectAttributes.memberId(), targetMemberInfo.schoolId())
            || isCentralCore(subjectAttributes.memberId());
    }

    private boolean canDeleteMember(SubjectAttributes subjectAttributes) {
        // 회원 강제 삭제는 중앙운영사무국 총괄단만 가능합니다.
        return getChallengerRoleUseCase.isCentralCore(subjectAttributes.memberId());
    }

    // ANOTHER PRIVATE METHOD

    private boolean isSelf(SubjectAttributes subject, ResourcePermission resource) {
        return Objects.equals(subject.memberId().toString(), resource.resourceId());
    }

    private boolean isSchoolCoreOf(Long memberId, Long schoolId) {
        return getChallengerRoleUseCase.isSchoolCore(memberId, schoolId);
    }

    private boolean isCentralCore(Long memberId) {
        return getChallengerRoleUseCase.isCentralCore(memberId);
    }
}
