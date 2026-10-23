package com.umc.product.blog.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.blog.adapter.in.web.dto.response.BlogCommentResponse.AuthorResponse;
import com.umc.product.blog.application.port.in.query.dto.BlogContentInfo;
import com.umc.product.blog.domain.BlogContentStatus;
import com.umc.product.blog.domain.BlogContentType;

public record BlogContentResponse(
    Long id,
    BlogContentType type,
    String slug,
    String title,
    String summary,
    String thumbnailUrl,
    String content,
    BlogContentStatus status,
    AuthorResponse author,
    Instant publishedAt,
    Instant updatedAt,
    String canonicalPath,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    List<BlogSeriesSummaryResponse> series,
    List<BlogHashtagResponse> hashtags,
    boolean canEdit,
    boolean canDelete
) {
    public static BlogContentResponse from(BlogContentInfo info) {
        return new BlogContentResponse(
            info.id(),
            info.type(),
            info.slug(),
            info.title(),
            info.summary(),
            info.thumbnailUrl(),
            info.content(),
            info.status(),
            AuthorResponse.from(info.author()),
            info.publishedAt(),
            info.updatedAt(),
            info.canonicalPath(),
            info.seoTitle(),
            info.seoDescription(),
            info.ogImageUrl(),
            info.series().stream().map(BlogSeriesSummaryResponse::from).toList(),
            info.hashtags().stream().map(BlogHashtagResponse::from).toList(),
            info.canEdit(),
            info.canDelete()
        );
    }
}
