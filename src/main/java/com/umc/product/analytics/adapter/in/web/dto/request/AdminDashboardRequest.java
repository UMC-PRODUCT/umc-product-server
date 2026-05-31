package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardQuery;

public record AdminDashboardRequest(
    Long gisuId,
    Long chapterId,
    Long schoolId
) {

    public AdminDashboardQuery toQuery(Long requesterMemberId) {
        return AdminDashboardQuery.of(requesterMemberId, gisuId, chapterId, schoolId);
    }
}
