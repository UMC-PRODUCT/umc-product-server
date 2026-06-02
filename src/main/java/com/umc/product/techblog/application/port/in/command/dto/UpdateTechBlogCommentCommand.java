package com.umc.product.techblog.application.port.in.command.dto;

public record UpdateTechBlogCommentCommand(
    String type,
    String slug,
    Long commentId,
    Long memberId,
    String content
) {

    public static UpdateTechBlogCommentCommand of(
        String type,
        String slug,
        Long commentId,
        Long memberId,
        String content
    ) {
        return new UpdateTechBlogCommentCommand(type, slug, commentId, memberId, content);
    }
}
