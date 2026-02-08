package com.umc.product.organization.application.port.in.query.dto;

import java.time.Instant;

public record SchoolDetailInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        String logoImageUrl,
        String kakaoLink,
        String instagramLink,
        String youtubeLink,
        Instant createdAt,
        Instant updatedAt
) {
    public record SchoolInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        String logoImageId,
        String kakaoLink,
        String instagramLink,
        String youtubeLink,
        Instant createdAt,
        Instant updatedAt
    ) {
        public SchoolDetailInfo toDetailInfo(String logoImageUrl) {
            return new SchoolDetailInfo(
                chapterId,
                chapterName,
                schoolName,
                schoolId,
                remark,
                logoImageUrl,
                kakaoLink,
                instagramLink,
                youtubeLink,
                createdAt,
                updatedAt
            );
        }
    }
}
