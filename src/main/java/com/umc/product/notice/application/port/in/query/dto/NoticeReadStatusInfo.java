package com.umc.product.notice.application.port.in.query.dto;


import com.umc.product.common.domain.enums.ChallengerPart;

public record NoticeReadStatusInfo(
        Long cursorId, /* 공지를 읽은 경우: NoticeReadId, 공지를 읽지 않은 경우: ChallengerId */
        Long challengerId,
        String name,
        String profileImageUrl,
        ChallengerPart part,
        String schoolName,
        String region,
        Boolean isRead
) {
}
