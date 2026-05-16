package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;
import java.time.Instant;

public record AdminOperationsOverviewRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsOverviewQuery toQuery(Long requesterMemberId) {
        return AdminOperationsOverviewQuery.of(requesterMemberId, gisuId, from, to);
    }
}
