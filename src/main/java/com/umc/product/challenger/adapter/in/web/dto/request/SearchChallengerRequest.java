package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.query.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerRequest(
        Long challengerId,
        String nickname,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        Long gisuId

) {

    public SearchChallengerQuery toQuery() {
        return new SearchChallengerQuery(
                challengerId,
                nickname,
                schoolId,
                chapterId,
                part,
                gisuId
        );
    }
}
