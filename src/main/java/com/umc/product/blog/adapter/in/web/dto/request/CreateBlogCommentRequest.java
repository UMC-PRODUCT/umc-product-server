package com.umc.product.blog.adapter.in.web.dto.request;

import com.umc.product.blog.application.port.in.command.dto.CreateBlogCommentCommand;

import jakarta.validation.constraints.Size;

public record CreateBlogCommentRequest(
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력할 수 있습니다.") String content,

    Long parentCommentId,

    Boolean anonymous,

    @Size(max = 20, message = "닉네임은 최대 20자까지 입력할 수 있습니다.") String nickname
) {

    public CreateBlogCommentRequest {
        content = normalize(content);
        nickname = normalize(nickname);
    }

    public CreateBlogCommentCommand toCommand(String type, String slug, Long memberId) {
        boolean anonymousValue = memberId == null || Boolean.TRUE.equals(anonymous);
        return CreateBlogCommentCommand.of(
            type,
            slug,
            parentCommentId,
            memberId,
            anonymousValue,
            nickname,
            content
        );
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
