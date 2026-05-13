package com.umc.product.analytics.application.port.in.query.dto;

public record AdminDashboardQuery(
    Long requesterMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId
) {

    public static AdminDashboardQuery of(Long requesterMemberId, Long gisuId, Long chapterId, Long schoolId) {
        return new AdminDashboardQuery(requesterMemberId, gisuId, chapterId, schoolId);
    }
}
