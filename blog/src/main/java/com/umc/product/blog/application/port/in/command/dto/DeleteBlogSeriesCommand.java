package com.umc.product.blog.application.port.in.command.dto;

public record DeleteBlogSeriesCommand(
    Long seriesId,
    Long memberId
) {
    public static DeleteBlogSeriesCommand of(Long seriesId, Long memberId) {
        return new DeleteBlogSeriesCommand(seriesId, memberId);
    }
}
