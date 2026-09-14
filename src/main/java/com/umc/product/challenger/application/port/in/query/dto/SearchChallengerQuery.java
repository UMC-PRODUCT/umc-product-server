package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.ChallengerTrack;

public record SearchChallengerQuery(
        Long challengerId,
        String name,
        String nickname,
        String keyword,
        Long schoolId,
        Long chapterId,
        ChallengerPart part,
        ChallengerTrack track,
        Long gisuId,
        List<ChallengerStatus> statuses
) {
    /**
     * track 필터 없이 조회하는 기존 호출부 호환용 생성자. track은 null로 둔다.
     */
    public SearchChallengerQuery(
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
        this(challengerId, name, nickname, keyword, schoolId, chapterId, part, null, gisuId, statuses);
    }
}
