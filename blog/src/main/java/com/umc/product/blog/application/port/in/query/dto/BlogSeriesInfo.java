package com.umc.product.blog.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.blog.domain.BlogContentType;

public record BlogSeriesInfo(
    Long id,
    BlogContentType type,
    String slug,
    String title,
    String description,
    String thumbnailUrl,
    BlogAuthorInfo author,
    int contentCount,
    Instant updatedAt,
    String canonicalPath,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    boolean canEdit,
    boolean canDelete
) {
}
