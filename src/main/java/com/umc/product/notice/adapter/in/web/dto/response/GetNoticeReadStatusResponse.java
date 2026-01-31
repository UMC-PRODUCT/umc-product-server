package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;

public record GetNoticeReadStatusResponse(
    Long challengerId,
    String name,
    String profileImageUrl,
    ChallengerPart part,
    String schoolName,
    String region,
    Boolean isRead
) {

    public static GetNoticeReadStatusResponse from(NoticeReadStatusInfo info) {
        return new GetNoticeReadStatusResponse(
            info.challengerId(),
            info.name(),
            info.profileImageUrl(),
            info.part(),
            info.schoolName(),
            info.region(),
            info.isRead()
        );

    }
}
