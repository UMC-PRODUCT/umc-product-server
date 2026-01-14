package com.umc.product.organization.application.port.in.query.dto;

public record SchoolDeleteSearchCondition(
        String keyword,
        Long chapterId
) {
}
