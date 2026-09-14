package com.umc.product.blog.application.port.in.command.dto;

public record UpdateBlogSeriesCommand(
    Long seriesId,
    String slug,
    String title,
    String description,
    String thumbnailUrl,
    String seoTitle,
    String seoDescription,
    String ogImageUrl
) {
    public static UpdateBlogSeriesCommand of(
        Long seriesId,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        String seoTitle,
        String seoDescription,
        String ogImageUrl
    ) {
        return new UpdateBlogSeriesCommand(seriesId, slug, title, description, thumbnailUrl, seoTitle, seoDescription,
            ogImageUrl);
    }
}
