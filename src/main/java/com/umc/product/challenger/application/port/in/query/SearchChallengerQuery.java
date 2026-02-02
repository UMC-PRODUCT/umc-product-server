package com.umc.product.challenger.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerQuery(
        Long challengerId,
        String nickname,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        Long gisuId
) {

}
