package com.umc.product.techblog.application.port.in.command.dto;

import com.umc.product.techblog.domain.TechBlogDomainException;
import com.umc.product.techblog.domain.TechBlogErrorCode;

public record CreateTechBlogCommentCommand(
    String type,
    String slug,
    Long authorMemberId,
    String content,
    Long parentCommentId,
    boolean anonymous,
    String nickname
) {

    public CreateTechBlogCommentCommand {
        nickname = normalizeNickname(nickname);
    }

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

    private static String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        String normalized = nickname.trim();
        if (normalized.length() > 20) {
            throw new TechBlogDomainException(TechBlogErrorCode.INVALID_NICKNAME);
        }
        return normalized;
    }
}
