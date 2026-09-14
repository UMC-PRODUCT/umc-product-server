package com.umc.product.notice.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.notice.domain.NoticeTargetInfo;

public record NoticeSummary(
    Long id,
    String title,
    String content,
    Boolean shouldSendNotification,
    boolean mustRead,
    Long viewCount,
    Instant createdAt,
    NoticeTargetInfo targetInfo,
    Long authorMemberId,
    String authorNickname,
    String authorName
) {
}
