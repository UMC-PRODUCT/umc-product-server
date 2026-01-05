package com.umc.product.command.organization.application.port.in.dto.response;

public record SchoolDeleteSearchCondition(
        String keyword,
        Long chapterId
) {
}
