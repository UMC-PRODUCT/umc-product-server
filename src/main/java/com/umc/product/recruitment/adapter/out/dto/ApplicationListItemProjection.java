package com.umc.product.recruitment.adapter.out.dto;

public record ApplicationListItemProjection(
    Long applicationId,
    Long applicantMemberId,
    String applicantName,
    String applicantNickname,
    boolean isEvaluated
) {
}
