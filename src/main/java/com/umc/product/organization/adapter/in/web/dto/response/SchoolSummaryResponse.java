package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolSummary;

public record SchoolSummaryResponse(
        Long schoolId,
        String schoolName
) {
    public static SchoolSummaryResponse from(SchoolSummary summary) {
        return new SchoolSummaryResponse(
                summary.schoolId(),
                summary.schoolName()
        );
    }
}
