package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationGroup;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationListStatus;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerDeactivationQuery(
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    String keyword,
    ChallengerDeactivationReason reason,
    ChallengerDeactivationListStatus status,
    ChallengerDeactivationGroup type
) {
    public SearchChallengerDeactivationQuery withScope(Long gisuId, Long schoolId) {
        return new SearchChallengerDeactivationQuery(gisuId, chapterId, schoolId, part, keyword, reason, status, type);
    }
}
