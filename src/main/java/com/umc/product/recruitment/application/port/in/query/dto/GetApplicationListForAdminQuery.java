package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;

public record GetApplicationListForAdminQuery(
        Long recruitmentId,
        Long chapterId,
        Long schoolId,
        PartOption part,
        String keyword,
        int page,
        int size,
        Long requesterId
) {
}
