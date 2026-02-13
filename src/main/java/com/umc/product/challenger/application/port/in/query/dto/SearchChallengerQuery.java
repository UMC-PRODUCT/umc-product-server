package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;

public record SearchChallengerQuery(
        Long challengerId,
        String name,
        String nickname,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        Long gisuId,
        List<ChallengerStatus> statuses
) {

}
