package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

public record SearchChallengerQuery(
        Long challengerId,
        String name,
        String nickname,
        String keyword,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        Long gisuId,
        List<ChallengerStatus> statuses
) {

}
