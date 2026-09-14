package com.umc.product.blog.application.port.in.command.dto;

public record DeleteBlogCommentCommand(
    String type,
    String slug,
    Long commentId,
    Long memberId
) {

    public static DeleteBlogCommentCommand of(String type, String slug, Long commentId, Long memberId) {
        return new DeleteBlogCommentCommand(type, slug, commentId, memberId);
    }
}
