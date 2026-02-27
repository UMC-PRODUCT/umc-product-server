package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateChallengerRecordRequest(
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    String memberName
) {
}
