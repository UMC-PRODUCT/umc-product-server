package com.umc.product.organization.application.port.in.query.dto.school;

public record SchoolSearchCondition(
    String keyword,
    Long chapterId
) {

}

