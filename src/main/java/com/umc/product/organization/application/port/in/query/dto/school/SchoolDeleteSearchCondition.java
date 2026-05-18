package com.umc.product.organization.application.port.in.query.dto.school;

public record SchoolDeleteSearchCondition(
    String keyword,
    Long chapterId
) {
}
