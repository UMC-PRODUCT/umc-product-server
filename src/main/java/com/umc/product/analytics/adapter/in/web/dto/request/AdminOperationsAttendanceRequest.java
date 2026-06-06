package com.umc.product.analytics.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsAttendanceQuery;

public record AdminOperationsAttendanceRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsAttendanceQuery toQuery(Long requesterMemberId) {
        return AdminOperationsAttendanceQuery.of(requesterMemberId, gisuId, from, to);
    }
}
