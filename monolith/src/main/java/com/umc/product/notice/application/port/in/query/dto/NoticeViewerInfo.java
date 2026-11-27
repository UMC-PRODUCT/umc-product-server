package com.umc.product.notice.application.port.in.query.dto;

import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.enums.NoticeTab;

/**
 * 공지 목록·검색에 적용할 조회자의 역할과 소속.
 * 직책을 선택한 경우 그 직책의 역할·소속·담당 파트를 사용합니다.
 * 직책을 생략하면 기존 계정의 최상위 역할과 본인의 수강·담당 파트를 사용합니다.
 */
public record NoticeViewerInfo(
    Set<ChallengerPart> memberParts,
    Long schoolId,
    Long chapterId,
    NoticeTab viewerRole,
    ChallengerRoleType roleType,
    Set<Long> chapterSchoolIds,
    Long schoolRoleSchoolId
) {
    public NoticeViewerInfo(Set<ChallengerPart> memberParts, Long schoolId, Long chapterId, NoticeTab viewerRole,
                            ChallengerRoleType roleType, Set<Long> chapterSchoolIds) {
        this(memberParts, schoolId, chapterId, viewerRole, roleType, chapterSchoolIds, null);
    }

    public NoticeViewerInfo(Set<ChallengerPart> memberParts, Long schoolId, Long chapterId, NoticeTab viewerRole) {
        this(memberParts, schoolId, chapterId, viewerRole, null, Set.of(), null);
    }

    public boolean isChapterPresident() {
        return roleType == ChallengerRoleType.CHAPTER_PRESIDENT;
    }
}
