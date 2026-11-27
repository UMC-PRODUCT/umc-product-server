package com.umc.product.notice.application.service;

import java.util.function.Function;

import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;

/** 공지 상세 조회와 수신 현황에서 동일하게 적용하는 열람 정책. */
public final class NoticeReadPolicy {

    private NoticeReadPolicy() {
    }

    public static boolean canRead(SubjectAttributes subjectAttributes, NoticeTargetInfo targetInfo,
                                  Function<Long, Long> chapterOfTargetSchoolInGisu) {
        // 총괄/부총괄: 모든 공지 읽기 가능
        if (subjectAttributes.toAuthoritySnapshot().isCentralCoreInAnyGisu()) {
            return true;
        }

        if (targetInfo.isStaffNotice()) {
            return canReadStaffNotice(subjectAttributes, targetInfo);
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

        // 역할 기반 추가 읽기 권한 체크
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> canReadByRole(role, targetInfo, subjectAttributes, chapterOfTargetSchoolInGisu));
    }

    private static boolean canReadStaffNotice(SubjectAttributes subjectAttributes, NoticeTargetInfo targetInfo) {
        // 교내 공지 열람은 모든 파트에 허용하되, 알림 수신자 선정에 쓰이는 isStaffTarget 규칙은 유지합니다.
        if (targetInfo.targetNoticeTab() == NoticeTab.SCHOOL_PART_LEADER
            && targetInfo.targetGisuId() != null && targetInfo.targetSchoolId() != null
            && subjectAttributes.toAuthoritySnapshot()
                .isSchoolAdminInGisu(targetInfo.targetGisuId(), targetInfo.targetSchoolId())) {
            return true;
        }
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> targetInfo.isStaffTarget(role.roleType(), role.gisuId(), role.organizationId(),
                role.responsiblePart()));
    }

    /**
     * 역할에 따른 추가 읽기 권한 평가
     * <p>
     * 기본 챌린저 체크(part 기반)를 통과하지 못한 경우에만 호출되도록 구현
     */
    private static boolean canReadByRole(RoleAttribute role, NoticeTargetInfo targetInfo, SubjectAttributes subject,
                                         Function<Long, Long> chapterOfTargetSchoolInGisu) {
        return switch (role.roleType()) {
            // 중앙운영진: 본인 기수 범위의 모든 챌린저 공지를 파트 무관하게 읽기 가능
            case CENTRAL_OPERATING_TEAM_MEMBER, CENTRAL_EDUCATION_TEAM_MEMBER -> targetInfo.targetGisuId() == null
                || targetInfo.targetGisuId().equals(role.gisuId());
            case CHAPTER_PRESIDENT -> chapterPresidentCanRead(role, targetInfo, chapterOfTargetSchoolInGisu);
            case SCHOOL_PRESIDENT, SCHOOL_VICE_PRESIDENT, SCHOOL_PART_LEADER, SCHOOL_ETC_ADMIN ->
                schoolCoreCanRead(role, targetInfo, subject);
            default -> false;
        };
    }

    private static boolean chapterPresidentCanRead(RoleAttribute role, NoticeTargetInfo targetInfo,
                                                   Function<Long, Long> chapterOfTargetSchoolInGisu) {
        Long myChapterId = role.organizationId();
        if (myChapterId == null
            || targetInfo.targetGisuId() != null && !role.gisuId().equals(targetInfo.targetGisuId())) {
            return false;
        }
        if (targetInfo.targetSchoolId() != null) {
            return myChapterId.equals(chapterOfTargetSchoolInGisu.apply(role.gisuId()));
        }
        return true;
    }

    private static boolean schoolCoreCanRead(RoleAttribute role, NoticeTargetInfo targetInfo,
                                             SubjectAttributes subject) {
        Long mySchoolId = role.organizationId();
        if (mySchoolId == null) {
            return false;
        }
        // 학교 대상 공지는 본인 학교에 한정. 지부/전체 범위 공지(targetSchoolId == null)는
        // 기수·지부 일치 여부로만 판정하여 회장단이 본인 지부공지를 파트 무관하게 읽을 수 있도록 함.
        if (targetInfo.targetSchoolId() != null && !mySchoolId.equals(targetInfo.targetSchoolId())) {
            return false;
        }
        return isInGisuAndChapter(role, targetInfo, subject);
    }

    private static boolean isInGisuAndChapter(RoleAttribute role, NoticeTargetInfo targetInfo,
                                              SubjectAttributes subject) {
        return subject.gisuChallengerInfos().stream()
            .filter(info -> role.gisuId().equals(info.gisuId()))
            .anyMatch(info ->
                (targetInfo.targetGisuId() == null || targetInfo.targetGisuId().equals(info.gisuId()))
                    && (targetInfo.targetChapterId() == null || targetInfo.targetChapterId().equals(info.chapterId())));
    }
}
