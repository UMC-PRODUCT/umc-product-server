package com.umc.product.organization.application.port.in.query.dto;

public record MemberSummaryInfo(
        Long challengerId,
        String name,
        String profileImageUrl) {
}
