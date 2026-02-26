package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.School;

public record UnassignedSchoolInfo(
        Long schoolId,
        String schoolName
) {
    public static UnassignedSchoolInfo from(School school) {
        return new UnassignedSchoolInfo(school.getId(), school.getName());
    }
}
