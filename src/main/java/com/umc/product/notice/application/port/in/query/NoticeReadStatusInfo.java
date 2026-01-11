package com.umc.product.notice.application.port.in.query;

import com.umc.product.challenger.domain.enums.ChallengerPart;

public record NoticeReadStatusInfo(
        Long challengerId,
        String name,
        String profileImageUrl,
        ChallengerPart part,
        String schoolName,
        String region,
        Boolean isRead
) {
}
