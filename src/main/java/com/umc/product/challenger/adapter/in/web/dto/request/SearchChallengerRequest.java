package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;

public record SearchChallengerRequest(
        Long challengerId,
        String name,
        String nickname,
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
                schoolId,
                chapterId,
                part,
                gisuId,
                List.of(ChallengerStatus.ACTIVE)
        );
    }
}
