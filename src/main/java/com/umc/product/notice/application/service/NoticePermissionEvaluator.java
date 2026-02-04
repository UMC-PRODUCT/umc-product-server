package com.umc.product.notice.application.service;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
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
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberRolesUseCase getMemberRolesUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.NOTICE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes,
                            ResourcePermission resourcePermission) {
        // WRITE는 별도로 지원하지 않음, Service에서 직접 확인함

        // ResourcePermission에 나와 있는 공지사항의 타겟 정보를 먼저 조회함
        NoticeTargetInfo targetInfo =
            getNoticeTargetUseCase.findByNoticeId(resourcePermission.getResourceIdAsLong());

        // READ인 경우, 해당 공지사항에 대한 타겟에 사용자가 해당하는지 확인
        if (resourcePermission.permission() == PermissionType.READ) {
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

        // DELETE는 작성자 본인만 가능함.
        else if (resourcePermission.permission() == PermissionType.DELETE) {
            NoticeInfo noticeInfo = getNoticeUseCase.getNoticeDetail(resourcePermission.getResourceIdAsLong());
            Long authorMemberId = getChallengerUseCase.getChallengerPublicInfo(noticeInfo.authorChallengerId())
                .memberId();

            if (Objects.equals(subjectAttributes.memberId(), authorMemberId)) {
                return true;
            }
        }

        // MANAGE는 공지 카테고리에 해당하는 운영진만 가능
        else if (resourcePermission.permission() == PermissionType.MANAGE) {
            return canManageNotice(subjectAttributes.memberId(), targetInfo);
        }

        throw new AuthorizationDomainException(AuthorizationErrorCode.INVALID_RESOURCE_PERMISSION_GIVEN,
            "NoticePermissionEvaluator애서 지원하지 않는 권한 유형에 대한 평가가 시도되었습니다: " + resourcePermission.permission());
    }

    /**
     * 공지사항 관리 권한 확인 (수신 현황 조회 등)
     * - 총괄/부총괄: 항상 허용
     * - School 레벨 공지: 해당 학교 운영진
     * - Chapter 레벨 공지: 해당 지부장
     * - Gisu 레벨 공지: 중앙 멤버
     */
    private boolean canManageNotice(Long memberId, NoticeTargetInfo targetInfo) {
        // 총괄/부총괄은 항상 허용
        if (getMemberRolesUseCase.isCentralCore(memberId)) {
            return true;
        }

        // School 레벨 공지: 해당 학교 운영진
        if (targetInfo.targetSchoolId() != null) {
            return getMemberRolesUseCase.isSchoolAdmin(memberId, targetInfo.targetSchoolId());
        }

        // Chapter 레벨 공지: 해당 지부장
        if (targetInfo.targetChapterId() != null) {
            return getMemberRolesUseCase.isChapterPresident(memberId, targetInfo.targetChapterId());
        }

        // Gisu 레벨 공지 (전체): 중앙 멤버
        return getMemberRolesUseCase.isCentralMember(memberId);
    }
}
