package com.umc.product.blog.application.port.in.command.dto;

import java.util.List;

import com.umc.product.blog.domain.BlogContentStatus;

public record UpdateBlogContentCommand(
    Long contentId,
    String slug,
    String title,
    String summary,
    String thumbnailUrl,
    String content,
    BlogContentStatus status,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    List<String> hashtags
) {
    public static UpdateBlogContentCommand of(
        Long contentId,
        String slug,
        String title,
        String summary,
        String thumbnailUrl,
        String content,
        BlogContentStatus status,
        String seoTitle,
        String seoDescription,
        String ogImageUrl,
        List<String> hashtags
    ) {
        return new UpdateBlogContentCommand(contentId, slug, title, summary, thumbnailUrl, content, status, seoTitle,
            seoDescription, ogImageUrl, hashtags == null ? List.of() : List.copyOf(hashtags));
    }
}
