package com.umc.product.analytics.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsQuery;

public record AdminOperationsPointsRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsPointsQuery toQuery(Long requesterMemberId) {
        return AdminOperationsPointsQuery.of(requesterMemberId, gisuId, from, to);
    }
}
