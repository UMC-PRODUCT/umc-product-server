package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;

import java.time.LocalDate;

public record SchoolResponse(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        LocalDate createdAt,
        LocalDate updatedAt
) {
    public static SchoolResponse from(SchoolInfo info) {
        return new SchoolResponse(
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
