package com.umc.product.notice.adapter.in.web.dto.response.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;

/**
 * 공지사항 수신 현황 확인 응답
 * <p>
 * Cursor 페이지네이션으로 응답함
 */
public record GetNoticeReadStatusResponse(
    Long challengerId,
    String name,
    String profileImageUrl,
    ChallengerPart part,
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    Boolean isRenotifiedMember
) {
    public static GetNoticeReadStatusResponse from(NoticeReadStatusInfo info) {
        return new GetNoticeReadStatusResponse(
            info.challengerId(),
            info.name(),
            info.profileImageUrl(),
            info.part(),
            info.schoolId(),
            info.schoolName(),
            info.chapterId(),
            info.chapterName(),
            info.isRenotifiedMember()
        );
    }
}
