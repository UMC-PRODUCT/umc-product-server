package com.umc.product.techblog.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TechBlogContent {

    private final Long id;
    private final TechBlogContentType type;
    private final String slug;

    public static TechBlogContent create(TechBlogContentType type, String slug) {
        validate(type, slug);
        return TechBlogContent.builder()
            .type(type)
            .slug(slug.trim())
            .build();
    }

    public static TechBlogContent reconstruct(Long id, TechBlogContentType type, String slug) {
        validate(type, slug);
        if (id == null || id <= 0) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_ID);
        }
        return TechBlogContent.builder()
            .id(id)
            .type(type)
            .slug(slug)
            .build();
    }

    private static void validate(TechBlogContentType type, String slug) {
        if (type == null) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_CONTENT_TYPE);
        }
        if (slug == null || slug.isBlank() || slug.length() > 200) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_SLUG);
        }
    }
}
