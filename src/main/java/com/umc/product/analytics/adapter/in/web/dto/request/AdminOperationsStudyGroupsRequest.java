package com.umc.product.analytics.adapter.in.web.dto.request;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsQuery;

public record AdminOperationsStudyGroupsRequest(
    Long gisuId,
    Instant from,
    Instant to
) {

    public AdminOperationsStudyGroupsQuery toQuery(Long requesterMemberId) {
        return AdminOperationsStudyGroupsQuery.of(requesterMemberId, gisuId, from, to);
    }
}
