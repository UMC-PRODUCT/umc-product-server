package com.umc.product.analytics.adapter.out.persistence.row;

import com.umc.product.common.domain.enums.ChallengerPart;

public record AdminRiskChallengerRow(
    Long challengerId,
    Long memberId,
    String name,
    String schoolName,
    ChallengerPart part,
    double pointSum
) {
}
