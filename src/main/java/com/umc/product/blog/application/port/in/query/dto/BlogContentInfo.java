package com.umc.product.blog.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;

public record BlogContentInfo(
    Long id,
    BlogContentType type,
    String slug,
    String title,
    String summary,
    String thumbnailUrl,
    String content,
    BlogContentStatus status,
    BlogAuthorInfo author,
    Instant publishedAt,
    Instant updatedAt,
    String canonicalPath,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    List<BlogSeriesSummaryInfo> series,
    List<BlogHashtagInfo> hashtags,
    boolean canEdit,
    boolean canDelete
) {
}
