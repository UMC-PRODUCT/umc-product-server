package com.umc.product.blog.adapter.in.web.dto.request;

import com.umc.product.blog.application.port.in.command.dto.UpdateBlogCommentCommand;

import jakarta.validation.constraints.Size;

public record UpdateBlogCommentRequest(
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력할 수 있습니다.") String content
) {

    public UpdateBlogCommentRequest {
        content = normalize(content);
    }

    public UpdateBlogCommentCommand toCommand(String type, String slug, Long commentId, Long memberId) {
        return UpdateBlogCommentCommand.of(type, slug, commentId, memberId, content);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
