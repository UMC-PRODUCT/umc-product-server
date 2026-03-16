package com.umc.product.notice.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
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
        // 총괄/부총괄: 모든 공지 읽기 가능
        if (subjectAttributes.roleAttributes().stream().anyMatch(r -> r.roleType().isAtLeastCentralCore())) {
            return true;
        }

        // 기본 챌린저 권한 체크 (본인의 part, gisu, chapter 기반)
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

        // 역할 기반 추가 권한 체크
        if (subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> canReadByRole(role, targetInfo, subjectAttributes))) {
            return true;
        }

        return false;
    }

    /**
     * 역할에 따른 추가 읽기 권한 평가
     * <p>
     * 기본 챌린저 체크(part 기반)를 통과하지 못한 경우에만 호출되도록 구현
     */
    private boolean canReadByRole(RoleAttribute role, NoticeTargetInfo targetInfo, SubjectAttributes subject) {
        return switch (role.roleType()) {
            // 중앙운영진(운영국원, 교육국원): 본인 기수 범위의 모든 공지를 파트 무관하게 읽기 가능
            case CENTRAL_OPERATING_TEAM_MEMBER, CENTRAL_EDUCATION_TEAM_MEMBER -> targetInfo.targetGisuId() == null ||
                targetInfo.targetGisuId().equals(role.gisuId());
            case CHAPTER_PRESIDENT -> chapterPresidentCanRead(role, targetInfo, subject);
            case SCHOOL_PRESIDENT, SCHOOL_VICE_PRESIDENT -> schoolCoreCanRead(role, targetInfo, subject);
            case SCHOOL_PART_LEADER -> schoolPartLeaderCanRead(role, targetInfo, subject);
            default -> false;
        };
    }


    private boolean canDeleteNotice(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (subjectAttributes.roleAttributes().stream().anyMatch(r -> r.roleType().isSuperAdmin())) {
            return true;
        }

        NoticeInfo noticeInfo = getNoticeUseCase.getNoticeDetail(resourcePermission.getResourceIdAsLong(),
            subjectAttributes.memberId());

        return Objects.equals(subjectAttributes.memberId(), noticeInfo.authorMemberId());
    }

    /**
     * 공지사항 관리 권한 확인 (수신 현황 조회 등) - 총괄/부총괄: 항상 허용 - School 레벨 공지: 해당 학교 운영진 - Chapter 레벨 공지: 해당 지부장 - Gisu 레벨 공지: 중앙 멤버
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

    /**
     * Role별 Read권한 검증
     */
    // 지부장: 본인 지부 또는 본인 학교 대상 공지를 파트 무관하게 읽기 가능
    private boolean chapterPresidentCanRead(RoleAttribute role, NoticeTargetInfo targetInfo,
                                            SubjectAttributes subject) {
        Long myChapterId = role.organizationId();
        if (myChapterId == null) {
            return false;
        }
        // 지부가 명시된 경우 본인 지부이어야 함
        if (targetInfo.targetChapterId() != null && !myChapterId.equals(targetInfo.targetChapterId())) {
            return false;
        }
        // 학교가 명시된 경우 본인 학교이어야 함
        if (targetInfo.targetSchoolId() != null
            && (subject.schoolId() == null || !subject.schoolId().equals(targetInfo.targetSchoolId()))) {
            return false;
        }
        // 지부·학교가 모두 미지정인 기수 범위 공지는 기본 체크에서 처리
        if (targetInfo.targetChapterId() == null && targetInfo.targetSchoolId() == null) {
            return false;
        }
        return subject.gisuChallengerInfos().stream()
            .filter(info -> myChapterId.equals(info.chapterId()))
            .filter(info -> role.gisuId().equals(info.gisuId()))
            .anyMatch(info -> targetInfo.targetGisuId() == null
                || targetInfo.targetGisuId().equals(info.gisuId()));
    }

    // 학교 회장단: 본인 학교 대상 공지를 파트 무관하게 읽기 가능
    private boolean schoolCoreCanRead(RoleAttribute role, NoticeTargetInfo targetInfo, SubjectAttributes subject) {
        Long mySchoolId = role.organizationId();
        if (mySchoolId == null) {
            return false;
        }
        if (!mySchoolId.equals(targetInfo.targetSchoolId())) {
            return false;
        }
        return isInGisuAndChapter(role, targetInfo, subject);
    }

    // 교내 파트장: 담당 파트 공지를 본인 학교 범위 내에서 읽기 가능
    private boolean schoolPartLeaderCanRead(RoleAttribute role, NoticeTargetInfo targetInfo,
                                            SubjectAttributes subject) {
        ChallengerPart responsiblePart = role.responsiblePart();
        if (responsiblePart == null) {
            return false;
        }
        Long mySchoolId = role.organizationId();
        if (mySchoolId == null) {
            return false;
        }
        if (targetInfo.targetSchoolId() != null && !mySchoolId.equals(targetInfo.targetSchoolId())) {
            return false;
        }
        if (targetInfo.targetParts() == null || targetInfo.targetParts().isEmpty()
            || !targetInfo.targetParts().contains(responsiblePart)) {
            return false;
        }
        return isInGisuAndChapter(role, targetInfo, subject);
    }

    private boolean isInGisuAndChapter(RoleAttribute role, NoticeTargetInfo targetInfo, SubjectAttributes subject) {
        return subject.gisuChallengerInfos().stream()
            .filter(info -> role.gisuId().equals(info.gisuId()))
            .anyMatch(info ->
                (targetInfo.targetGisuId() == null || targetInfo.targetGisuId().equals(info.gisuId()))
                    && (targetInfo.targetChapterId() == null || targetInfo.targetChapterId().equals(info.chapterId())));
    }

}
