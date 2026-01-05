package com.umc.product.query.organization.application.port.in.dto.response;

public record SchoolDeleteSearchCondition(
        String keyword,
        Long chapterId
) {
}
