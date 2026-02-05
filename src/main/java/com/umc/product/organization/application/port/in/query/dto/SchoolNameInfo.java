package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.School;

public record SchoolNameInfo(
        Long schoolId,
        String schoolName
) {
    public static SchoolNameInfo from(School school) {
        return new SchoolNameInfo(school.getId(), school.getName());
    }
}
