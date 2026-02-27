package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import lombok.Builder;

@Builder
public record ChallengerRecordResponse(
    String code,
    ChallengerPart part,
    Long gisuId,
    Long gisu,
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    String memberName
) {
}
