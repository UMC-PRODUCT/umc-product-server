package com.umc.product.notice.application.port.in.query.dto;


import com.umc.product.common.domain.enums.ChallengerPart;

public record NoticeReadStatusInfo(
    Long challengerId,
    String name,
    String profileImageUrl,
    ChallengerPart part,
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName
) {
}
