package com.umc.product.organization.application.port.in.query.dto;

import java.time.Instant;

public record SchoolListItemInfo(
        Long schoolId,
        String schoolName,
        Long chapterId,      // 활성 기수에 속하지 않으면 null
        String chapterName,  // 활성 기수에 속하지 않으면 null
        Instant createdAt,
        boolean isActive     // 활성 기수의 ChapterSchool 존재 여부
) {

}
