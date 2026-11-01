package com.umc.product.blog.application.port.in.command.dto;

public record DeleteBlogContentCommand(
    Long contentId,
    Long memberId
) {
    public static DeleteBlogContentCommand of(Long contentId, Long memberId) {
        return new DeleteBlogContentCommand(contentId, memberId);
    }
}
