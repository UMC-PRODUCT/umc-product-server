package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.NoticeTargetInfo;
import java.time.Instant;

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
