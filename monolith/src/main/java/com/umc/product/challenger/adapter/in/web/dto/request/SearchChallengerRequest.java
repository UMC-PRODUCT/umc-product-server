package com.umc.product.challenger.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

public record SearchChallengerRequest(
        Long challengerId,
        String name,
        String nickname,
        String keyword,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        Long gisuId

) {

    public SearchChallengerQuery toQuery() {
        return new SearchChallengerQuery(
                challengerId,
                name,
                nickname,
                keyword,
                schoolId,
                chapterId,
                part,
                gisuId,
                List.of(ChallengerStatus.ACTIVE)
        );
    }
}
