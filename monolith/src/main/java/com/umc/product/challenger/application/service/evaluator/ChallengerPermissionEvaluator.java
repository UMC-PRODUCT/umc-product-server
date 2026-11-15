package com.umc.product.challenger.application.service.evaluator;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengerPermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

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
            case MANAGE -> canManageChallenger(subjectAttributes, resourcePermission);
            default -> throw new CommonException(CommonErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED); // 지원하지 않는 권한 유형은 거부
        };
    }

    private boolean canReadChallenger(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        ChallengerInfo targetChallenger = getChallengerUseCase.getById(resourcePermission.getResourceIdAsLong());

        Long targetGisuId = targetChallenger.gisuId();
        Long targetSchoolId = getMemberUseCase.getById(targetChallenger.memberId()).schoolId();
        AuthoritySnapshot snapshot = subjectAttributes.toAuthoritySnapshot();

        // SUPER_ADMIN 또는 대상 기수의 중앙운영사무국 소속
        if (snapshot.isCentralMemberInGisu(targetGisuId)) {
            return true;
        }

        if (targetSchoolId == null) {
            return false;
        }

        // 대상 기수에서 대상과 같은 학교의 교내 운영진
        if (snapshot.isSchoolAdminInGisu(targetGisuId, targetSchoolId)) {
            return true;
        }

        // 대상 기수에서 대상 학교가 속한 지부의 지부장
        return getChapterUseCase.findByGisuAndSchool(targetGisuId, targetSchoolId)
            .map(ChapterInfo::id)
            .map(chapterId -> snapshot.isChapterPresidentInGisu(targetGisuId, chapterId))
            .orElse(false);
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

    private boolean canManageChallenger(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (canDeleteChallenger(subjectAttributes)) {
            return true;
        }

        if (resourcePermission.resourceId() == null) {
            return false;
        }

        ChallengerInfo targetChallenger = getChallengerUseCase.getById(resourcePermission.getResourceIdAsLong());
        Long targetGisuId = targetChallenger.gisuId();
        AuthoritySnapshot snapshot = subjectAttributes.toAuthoritySnapshot();

        if (snapshot.isCentralMemberInGisu(targetGisuId)) {
            return true;
        }

        Long targetSchoolId = getMemberUseCase.getById(targetChallenger.memberId()).schoolId();
        return targetSchoolId != null && snapshot.isSchoolCoreInGisu(targetGisuId, targetSchoolId);
    }
}
