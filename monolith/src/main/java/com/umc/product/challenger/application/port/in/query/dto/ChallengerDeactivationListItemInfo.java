package com.umc.product.challenger.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationListStatus;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

public record ChallengerDeactivationListItemInfo(
    Long challengerId,
    Long deactivationId,
    String name,
    String nickname,
    String profileImageId,
    Long gisuId,
    Long schoolId,
    String schoolName,
    ChallengerPart part,
    ChallengerDeactivationListStatus status,
    ChallengerStatus challengerStatus,
    ChallengerDeactivationReason reason,
    Instant registeredAt
) {
}
