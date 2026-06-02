package com.umc.product.techblog.application.port.in.command.dto;

public record CreateTechBlogCommentCommand(
    String type,
    String slug,
    Long authorMemberId,
    String content,
    Long parentCommentId,
    boolean anonymous,
    String nickname
) {

    public static CreateTechBlogCommentCommand of(
        String type,
        String slug,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String nickname,
        String content
    ) {
        return new CreateTechBlogCommentCommand(
            type,
            slug,
            authorMemberId,
            content,
            parentCommentId,
            anonymous,
            nickname
        );
    }
}
