package com.umc.product.blog.application.port.in.command.dto;

import com.umc.product.blog.domain.BlogDomainException;
import com.umc.product.blog.domain.BlogErrorCode;

public record CreateBlogCommentCommand(
    String type,
    String slug,
    Long authorMemberId,
    String content,
    Long parentCommentId,
    boolean anonymous,
    String nickname
) {

    public CreateBlogCommentCommand {
        nickname = normalizeNickname(nickname);
    }

    public static CreateBlogCommentCommand of(
        String type,
        String slug,
        Long parentCommentId,
        Long authorMemberId,
        boolean anonymous,
        String nickname,
        String content
    ) {
        return new CreateBlogCommentCommand(
            type,
            slug,
            authorMemberId,
            content,
            parentCommentId,
            anonymous,
            nickname
        );
    }

    private static String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        String normalized = nickname.trim();
        if (normalized.length() > 20) {
            throw new BlogDomainException(BlogErrorCode.INVALID_NICKNAME);
        }
        return normalized;
    }
}
