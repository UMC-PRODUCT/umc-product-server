package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueQuery;

public record AdminDashboardActionQueueRequest(
    Long gisuId,
    Integer riskThreshold
) {

    public AdminDashboardActionQueueQuery toQuery(Long requesterMemberId) {
        return AdminDashboardActionQueueQuery.of(requesterMemberId, gisuId, riskThreshold);
    }
}
