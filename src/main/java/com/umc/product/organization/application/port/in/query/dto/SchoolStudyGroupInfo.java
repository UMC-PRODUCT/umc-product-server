package com.umc.product.organization.application.port.in.query.dto;

public record SchoolStudyGroupInfo(
        Long schoolId,
        String schoolName,
        String logoImageId,
        int totalStudyGroupCount,
        int totalMemberCount
) {
}
