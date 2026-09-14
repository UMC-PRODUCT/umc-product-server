package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;

public record AdminRiskChallengerResponse(
    Long challengerId,
    Long memberId,
    String name,
    String schoolName,
    ChallengerPart part,
    double pointSum,
    LatestNegativePointResponse latestNegativePoint
) {

    public static AdminRiskChallengerResponse from(AdminRiskChallengerInfo info) {
        return new AdminRiskChallengerResponse(
            info.challengerId(),
            info.memberId(),
            info.name(),
            info.schoolName(),
            info.part(),
            info.pointSum(),
            LatestNegativePointResponse.from(info.latestNegativePoint())
        );
    }

    public record LatestNegativePointResponse(
        PointType pointType,
        Instant createdAt,
        double score
    ) {

        public static LatestNegativePointResponse from(AdminRiskChallengerInfo.LatestNegativePointInfo info) {
            return info == null ? null : new LatestNegativePointResponse(
                info.pointType(),
                info.createdAt(),
                info.score()
            );
        }
    }
}
