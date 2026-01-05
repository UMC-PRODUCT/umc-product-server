package com.umc.product.organization.application.port.in.dto;

public record SchoolDeleteSearchCondition(
        String keyword,
        Long chapterId
) {
}
