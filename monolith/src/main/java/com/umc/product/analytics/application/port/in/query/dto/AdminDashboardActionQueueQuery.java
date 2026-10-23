package com.umc.product.analytics.application.port.in.query.dto;

public record AdminDashboardActionQueueQuery(
    Long requesterMemberId,
    Long gisuId,
    int riskThreshold
) {

    public static AdminDashboardActionQueueQuery of(Long requesterMemberId, Long gisuId, Integer riskThreshold) {
        return new AdminDashboardActionQueueQuery(
            requesterMemberId,
            gisuId,
            riskThreshold != null ? riskThreshold : -8
        );
    }
}
