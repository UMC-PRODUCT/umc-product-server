package com.umc.product.notice.application.service;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Notice(공지사항) 리소스에 대한 권한 평가
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NoticePermissionEvaluator implements ResourcePermissionEvaluator {

    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final LoadNoticePort loadNoticePort;
    private final GetChapterUseCase getChapterUseCase;

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

        NoticeTargetInfo targetInfo =
            getNoticeTargetUseCase.findByNoticeId(resourcePermission.getResourceIdAsLong());

        return switch (resourcePermission.permission()) {
            case READ -> NoticeReadPolicy.canRead(subjectAttributes, targetInfo, gisuId ->
                getChapterUseCase.findByGisuAndSchool(gisuId, targetInfo.targetSchoolId())
                    .map(ChapterInfo::id).orElse(null));
            case EDIT, DELETE -> canDeleteOrEditNotice(subjectAttributes, resourcePermission);
            // TODO: Check는 임시로 Manage랑 동일하게 적용, 하나야 수정해줘!
            case MANAGE, CHECK -> canManageNotice(subjectAttributes, targetInfo);
            default -> throw new AuthorizationDomainException(AuthorizationErrorCode.PERMISSION_TYPE_NOT_IMPLEMENTED,
                "NoticePE에서 해당 PermissionType을 지원하지 않습니다: " + resourcePermission.permission());
        };
    }

    private boolean canDeleteOrEditNotice(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (subjectAttributes.toAuthoritySnapshot().isSuperAdmin()) {
            return true;
        }

        Notice notice = loadNoticePort.findNoticeById(resourcePermission.getResourceIdAsLong())
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));

        return Objects.equals(subjectAttributes.memberId(), notice.getAuthorMemberId());
    }

    /**
     * 공지사항 관리 권한 확인 (수신 현황 조회 등) - 총괄/부총괄: 항상 허용 - School 레벨 공지: 해당 학교 운영진 - Chapter 레벨 공지: 해당 지부장 - Gisu 레벨 공지: 중앙 멤버
     */
    private boolean canManageNotice(SubjectAttributes subjectAttributes, NoticeTargetInfo targetInfo) {
        AuthoritySnapshot snapshot = subjectAttributes.toAuthoritySnapshot();
        if (snapshot.isCentralCoreInAnyGisu()) {
            return true;
        }

        if (targetInfo.targetSchoolId() != null) {
            return snapshot.isSchoolAdminInAnyGisu(targetInfo.targetSchoolId());
        }

        if (targetInfo.targetChapterId() != null) {
            return snapshot.isChapterPresidentInAnyGisu(targetInfo.targetChapterId());
        }

        return snapshot.isCentralMemberInAnyGisu();
    }
}
