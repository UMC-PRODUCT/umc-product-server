package com.umc.product.blog.application.port.in.command.dto;

import java.util.List;

import com.umc.product.blog.domain.BlogContentStatus;

public record CreateBlogContentCommand(
    String type,
    String slug,
    String title,
    String summary,
    String thumbnailUrl,
    String content,
    BlogContentStatus status,
    Long authorMemberId,
    String seoTitle,
    String seoDescription,
    String ogImageUrl,
    List<String> hashtags
) {
    public static CreateBlogContentCommand of(
        String type,
        String slug,
        String title,
        String summary,
        String thumbnailUrl,
        String content,
        BlogContentStatus status,
        Long authorMemberId,
        String seoTitle,
        String seoDescription,
        String ogImageUrl,
        List<String> hashtags
    ) {
        return new CreateBlogContentCommand(type, slug, title, summary, thumbnailUrl, content, status, authorMemberId,
            seoTitle, seoDescription, ogImageUrl, hashtags == null ? List.of() : List.copyOf(hashtags));
    }
}
