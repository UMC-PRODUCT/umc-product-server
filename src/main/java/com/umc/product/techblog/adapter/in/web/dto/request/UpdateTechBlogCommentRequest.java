package com.umc.product.techblog.adapter.in.web.dto.request;

import com.umc.product.techblog.application.port.in.command.dto.UpdateTechBlogCommentCommand;
import jakarta.validation.constraints.Size;

public record UpdateTechBlogCommentRequest(
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력할 수 있습니다.")
    String content
) {

    public UpdateTechBlogCommentRequest {
        content = normalize(content);
    }

    public UpdateTechBlogCommentCommand toCommand(String type, String slug, Long commentId, Long memberId) {
        return UpdateTechBlogCommentCommand.of(type, slug, commentId, memberId, content);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
