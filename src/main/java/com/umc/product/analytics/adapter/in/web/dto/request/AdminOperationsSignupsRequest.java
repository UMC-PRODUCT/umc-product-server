package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsQuery;
import java.time.Instant;

public record AdminOperationsSignupsRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsSignupsQuery toQuery(Long requesterMemberId) {
        return AdminOperationsSignupsQuery.of(requesterMemberId, gisuId, from, to);
    }
}
