package com.umc.product.notice.adapter.in.web.dto.response.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.dto.NoticeTargetInfo;
import java.time.Instant;
import lombok.Builder;

/**
 * 공지사항 리스트 조회 시 반환할 DTO
 * <p>
 * 축약되어 있음
 */
@Builder
public record GetNoticeSummaryResponse(
    Long id,
    String title,
    String content,
    // UI에서 빨간 점 표시 용; 알람을 전송하는 것인지에 대한 내용
    Boolean shouldSendNotification,
    Integer viewCount,
    Instant createdAt,
    NoticeTargetInfo targetInfo,
    Long authorChallengerId,
    String authorNickname,
    String authorName
) {
    public static GetNoticeSummaryResponse from(
        NoticeSummary noticeSummary
    ) {
        return GetNoticeSummaryResponse.builder()
            .id(noticeSummary.id())
            .title(noticeSummary.title())
            .content(noticeSummary.content())
            .shouldSendNotification(noticeSummary.shouldSendNotification())
            .viewCount(noticeSummary.viewCount())
            .createdAt(noticeSummary.createdAt())
            .targetInfo(noticeSummary.targetInfo())
            .authorChallengerId(noticeSummary.authorChallengerId())
            .authorNickname(noticeSummary.authorNickname())
            .authorName(noticeSummary.authorName())
            .build();
    }
}
