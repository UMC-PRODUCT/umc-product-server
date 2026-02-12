package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.SchoolLinkType;
import java.time.Instant;
import java.util.List;

public record SchoolDetailInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        String logoImageUrl,
        List<SchoolLinkItem> links,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
    public record SchoolLinkItem(
            String title,
            SchoolLinkType type,
            String url
    ) {
    }

    public record SchoolInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        String logoImageId,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
    ) {
        public SchoolDetailInfo toDetailInfo(String logoImageUrl, List<SchoolLinkItem> links) {
            return new SchoolDetailInfo(
                chapterId,
                chapterName,
                schoolName,
                schoolId,
                remark,
                logoImageUrl,
                links,
                isActive,
                createdAt,
                updatedAt
            );
        }
    }
}
