package com.umc.product.analytics.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;

public record AdminRiskChallengerInfo(
    Long challengerId,
    Long memberId,
    String name,
    String schoolName,
    ChallengerPart part,
    double pointSum,
    LatestNegativePointInfo latestNegativePoint
) {

    public static AdminRiskChallengerInfo of(
        Long challengerId,
        Long memberId,
        String name,
        String schoolName,
        ChallengerPart part,
        double pointSum,
        LatestNegativePointInfo latestNegativePoint
    ) {
        return new AdminRiskChallengerInfo(
            challengerId,
            memberId,
            name,
            schoolName,
            part,
            pointSum,
            latestNegativePoint
        );
    }

    public record LatestNegativePointInfo(
        PointType pointType,
        Instant createdAt,
        double score
    ) {

        public static LatestNegativePointInfo of(PointType pointType, Instant createdAt, double score) {
            return new LatestNegativePointInfo(pointType, createdAt, score);
        }
    }
}
