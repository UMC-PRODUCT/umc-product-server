package com.umc.product.analytics.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsQuery;

public record AdminOperationsSignupsRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsSignupsQuery toQuery(Long requesterMemberId) {
        return AdminOperationsSignupsQuery.of(requesterMemberId, gisuId, from, to);
    }
}
