package com.umc.product.organization.application.port.in.dto;

public record SchoolOptionQuery(
        Long schoolId,
        String schoolName
) {
}
