package com.umc.product.community.application.port.in.command.comment.dto;

import java.util.Objects;

public record CreateCommentCommand(
    Long postId,
    Long challengerId,
    String content,
    Long parentId  // null이면 원댓글
) {
    public CreateCommentCommand {
        Objects.requireNonNull(postId, "게시글 ID는 필수입니다");
        Objects.requireNonNull(challengerId, "챌린저 ID는 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");

        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
    }
}
