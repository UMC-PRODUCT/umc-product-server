package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;

import java.time.LocalDate;

public record SchoolDetailResponse(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        LocalDate createdAt,
        LocalDate updatedAt
) {
    public static SchoolDetailResponse from(SchoolInfo info) {
        return new SchoolDetailResponse(
                info.chapterId(),
                info.chapterName(),
                info.schoolName(),
                info.schoolId(),
                info.remark(),
                info.createdAt(),
                info.updatedAt()
        );
    }
}
