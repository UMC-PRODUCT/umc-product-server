package com.umc.product.notice.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.dto.NoticeTargetInfo;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Notice(공지사항) 리소스에 대한 권한 평가
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NoticePermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final GetNoticeUseCase getNoticeUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.NOTICE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes,
                            ResourcePermission resourcePermission) {
        if (!resourcePermission.resourceType().getSupportedPermissions()
            .contains(resourcePermission.permission())) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_RESOURCE_PERMISSION_GIVEN,
                "NoticePermissionEvaluator에서 지원하지 않는 권한 유형에 대한 평가가 시도되었습니다: " + resourcePermission.permission());
        }

        // WRITE는 별도로 지원하지 않음, Service에서 직접 확인함

        // ResourcePermission에 나와 있는 공지사항의 타겟 정보를 먼저 조회함
        NoticeTargetInfo targetInfo =
            getNoticeTargetUseCase.findByNoticeId(resourcePermission.getResourceIdAsLong());

        return switch (resourcePermission.permission()) {
            case READ -> canReadNotice(subjectAttributes, targetInfo);
            case EDIT, DELETE -> canDeleteNotice(subjectAttributes, resourcePermission);
            // TODO: Check는 임시로 Manage랑 동일하게 적용, 하나야 수정해줘!
            case MANAGE, CHECK -> canManageNotice(subjectAttributes.memberId(), targetInfo);
            default -> throw new AuthorizationDomainException(AuthorizationErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED,
                "NoticePE에서 해당 PermissionType을 지원하지 않습니다: " + resourcePermission.permission());
        };

    }

    private boolean canReadNotice(SubjectAttributes subjectAttributes, NoticeTargetInfo targetInfo) {
        for (GisuChallengerInfo gisuChallengerInfo : subjectAttributes.gisuChallengerInfos()) {
            if (targetInfo.isTarget(
                gisuChallengerInfo.gisuId(),
                gisuChallengerInfo.chapterId(),
                subjectAttributes.schoolId(),
                gisuChallengerInfo.part()
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean canDeleteNotice(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        NoticeInfo noticeInfo = getNoticeUseCase.getNoticeDetail(resourcePermission.getResourceIdAsLong(),
            subjectAttributes.memberId());

        return Objects.equals(subjectAttributes.memberId(), noticeInfo.authorMemberId());
    }

    /**
     * 공지사항 관리 권한 확인 (수신 현황 조회 등)
     * <p>
     * - 총괄/부총괄: 항상 허용
     * <p>
     * - School 레벨 공지: 해당 학교 운영진
     * <p>
     * - Chapter 레벨 공지: 해당 지부장
     * <p>
     * - Gisu 레벨 공지: 중앙 멤버
     */
    private boolean canManageNotice(Long memberId, NoticeTargetInfo targetInfo) {
        // 총괄/부총괄은 항상 허용
        if (getChallengerRoleUseCase.isCentralCore(memberId)) {
            return true;
        }

        // School 레벨 공지: 해당 학교 운영진
        if (targetInfo.targetSchoolId() != null) {
            return getChallengerRoleUseCase.isSchoolAdmin(memberId, targetInfo.targetSchoolId());
        }

        // Chapter 레벨 공지: 해당 지부장
        if (targetInfo.targetChapterId() != null) {
            return getChallengerRoleUseCase.isChapterPresident(memberId, targetInfo.targetChapterId());
        }

        // Gisu 레벨 공지 (전체): 중앙 멤버
        return getChallengerRoleUseCase.isCentralMember(memberId);
    }
}
