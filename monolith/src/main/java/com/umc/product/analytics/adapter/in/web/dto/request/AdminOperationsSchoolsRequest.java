package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsQuery;

public record AdminOperationsSchoolsRequest(Long gisuId) {

    public AdminOperationsSchoolsQuery toQuery(Long requesterMemberId) {
        return AdminOperationsSchoolsQuery.of(requesterMemberId, gisuId);
    }
}
