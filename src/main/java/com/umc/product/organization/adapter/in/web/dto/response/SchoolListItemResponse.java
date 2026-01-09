package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import java.time.LocalDate;

public record SchoolListItemResponse(Long schoolId, String schoolName, Long chapterId, String chapterName,
                                     LocalDate createdAt, boolean isActive) {

    public static SchoolListItemResponse of(SchoolListItemInfo summary) {
        return new SchoolListItemResponse(summary.schoolId(), summary.schoolName(), summary.chapterId(),
                summary.chapterName(), summary.createdAt(), summary.isActive());
    }
}

