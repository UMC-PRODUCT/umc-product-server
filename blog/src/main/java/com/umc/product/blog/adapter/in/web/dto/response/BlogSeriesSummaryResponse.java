package com.umc.product.blog.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.blog.adapter.in.web.dto.response.BlogCommentResponse.AuthorResponse;
import com.umc.product.blog.application.port.in.query.dto.BlogSeriesSummaryInfo;
import com.umc.product.blog.domain.BlogContentType;

public record BlogSeriesSummaryResponse(
    Long id,
    BlogContentType type,
    String slug,
    String title,
    String description,
    String thumbnailUrl,
    AuthorResponse author,
    int contentCount,
    Instant updatedAt,
    String canonicalPath,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    boolean canEdit,
    boolean canDelete
) {
    public static BlogSeriesSummaryResponse from(BlogSeriesSummaryInfo info) {
        return new BlogSeriesSummaryResponse(
            info.id(),
            info.type(),
            info.slug(),
            info.title(),
            info.description(),
            info.thumbnailUrl(),
            AuthorResponse.from(info.author()),
            info.contentCount(),
            info.updatedAt(),
            info.canonicalPath(),
            info.seoTitle(),
            info.seoDescription(),
            info.ogImageUrl(),
            info.canEdit(),
            info.canDelete()
        );
    }
}
