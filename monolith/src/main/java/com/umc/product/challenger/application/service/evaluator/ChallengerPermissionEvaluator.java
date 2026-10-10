package com.umc.product.challenger.application.service.evaluator;

import com.umc.product.authorization.domain.AuthoritySnapshot;
import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengerPermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.CHALLENGER;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> canReadChallenger(subjectAttributes, resourcePermission);
            case WRITE -> canCreateChallenger(subjectAttributes);
            case EDIT -> canUpdateChallenger(subjectAttributes, resourcePermission);
            case DELETE -> canDeleteChallenger(subjectAttributes);
            default -> throw new CommonException(CommonErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED); // 지원하지 않는 권한 유형은 거부
        };
    }

    private boolean canReadChallenger(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        ChallengerInfo targetChallenger = getChallengerUseCase.getById(resourcePermission.getResourceIdAsLong());

        Long targetGisuId = targetChallenger.gisuId();
        Long targetSchoolId = getMemberUseCase.getById(targetChallenger.memberId()).schoolId();
        AuthoritySnapshot snapshot = subjectAttributes.toAuthoritySnapshot();

        // 요청자가 SUPER_ADMIN이거나 대상 기수의 중앙운영사무국 소속인지 확인
        if (snapshot.isCentralMemberInGisu(targetGisuId)) {
            return true;
        }

        // 요청자가 대상 기수에서 대상과 같은 학교의 회장단인지 확인
        if (targetSchoolId != null) {
            return snapshot.isSchoolCoreInGisu(targetGisuId, targetSchoolId);
        }

        return false;
    }

    private boolean canCreateChallenger(SubjectAttributes subjectAttributes) {
        // 교내 회장/부회장 이상만 가능함
        return subjectAttributes.toAuthoritySnapshot().isSuperAdmin()
            || subjectAttributes.roleAttributes().stream()
            .anyMatch(roleAttribute -> roleAttribute.roleType().isAtLeastSchoolCore());
    }

    private boolean canUpdateChallenger(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        // 중앙운영사무국 총괄단만 가능
        return subjectAttributes.toAuthoritySnapshot().isCentralCoreInAnyGisu();
    }

    private boolean canDeleteChallenger(SubjectAttributes subjectAttributes) {
        // 중앙운영사무국 총괄단만 가능함
        return subjectAttributes.toAuthoritySnapshot().isCentralCoreInAnyGisu();
    }
}
