package com.umc.product.analytics.application.port.in.query.dto;

public record AdminOperationsSchoolsQuery(
    Long requesterMemberId,
    Long gisuId
) {

    public static AdminOperationsSchoolsQuery of(Long requesterMemberId, Long gisuId) {
        return new AdminOperationsSchoolsQuery(requesterMemberId, gisuId);
    }
}
