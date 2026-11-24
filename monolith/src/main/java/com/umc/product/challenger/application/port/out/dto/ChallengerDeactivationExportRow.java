package com.umc.product.challenger.application.port.out.dto;

import java.time.Instant;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

public record ChallengerDeactivationExportRow(
    String schoolName,
    ChallengerPart part,
    ChallengerStatus status,
    ChallengerDeactivationReason reason,
    String reasonDetail,
    Instant registeredAt,
    String memo,
    String name,
    String nickname
) {
}
