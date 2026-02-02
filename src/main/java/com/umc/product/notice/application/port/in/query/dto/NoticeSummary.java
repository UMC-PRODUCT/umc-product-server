package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.dto.NoticeTargetInfo;
import java.time.Instant;

public record NoticeSummary(
    Long id,
    String title,
    String content,
    Boolean shouldSendNotification,
    Integer viewCount,
    Instant createdAt,
    NoticeTargetInfo targetInfo,
    Long authorChallengerId,
    String authorNickname,
    String authorName
) {
}
