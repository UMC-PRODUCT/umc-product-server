package com.umc.product.techblog.application.port.in.command.dto;

public record DeleteTechBlogCommentCommand(
    String type,
    String slug,
    Long commentId,
    Long memberId
) {

    public static DeleteTechBlogCommentCommand of(String type, String slug, Long commentId, Long memberId) {
        return new DeleteTechBlogCommentCommand(type, slug, commentId, memberId);
    }
}
