package com.umc.product.blog.application.port.in.command.dto;

public record UpdateBlogCommentCommand(
    String type,
    String slug,
    Long commentId,
    Long memberId,
    String content
) {

    public static UpdateBlogCommentCommand of(
        String type,
        String slug,
        Long commentId,
        Long memberId,
        String content
    ) {
        return new UpdateBlogCommentCommand(type, slug, commentId, memberId, content);
    }
}
