package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.Command.CreateCommentCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용은 필수입니다")
        String content,

        Long parentId
) {
    public CreateCommentCommand toCommand(Long postId, Long challengerId) {
        return new CreateCommentCommand(postId, challengerId, content, parentId);
    }
}
