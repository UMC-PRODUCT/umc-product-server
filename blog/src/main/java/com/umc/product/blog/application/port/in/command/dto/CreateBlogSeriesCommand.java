package com.umc.product.blog.application.port.in.command.dto;

public record CreateBlogSeriesCommand(
    String type,
    String slug,
    String title,
    String description,
    String thumbnailUrl,
    Long authorMemberId,
    String seoTitle,
    String seoDescription,
    String ogImageUrl
) {
    public static CreateBlogSeriesCommand of(
        String type,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        Long authorMemberId,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        return new CreateBlogSeriesCommand(type, slug, title, description, thumbnailUrl, authorMemberId, seoTitle,
            seoDescription, ogImageUrl);
    }
}
