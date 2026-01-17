package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.Chapter;

public record ChapterInfo(Long id, String name) {

    public static ChapterInfo from(Chapter chapter) {
        return new ChapterInfo(chapter.getId(), chapter.getName());
    }
}
