package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;

public record GetNoticeReadStatusResponse(
        Long challengerId,
        String name,
        String profileImageUrl,
        ChallengerPart part,
        String schoolName,
        String region,
        Boolean isRead
) {
}
