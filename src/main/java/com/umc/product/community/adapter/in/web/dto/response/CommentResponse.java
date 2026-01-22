package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.CommentInfo;
import java.time.Instant;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long challengerId,
        String challengerName,
        String content,
        Instant createdAt
) {
    public static CommentResponse from(CommentInfo info) {
        return new CommentResponse(
                info.commentId(),
                info.postId(),
                info.challengerId(),
                info.challengerName(),
                info.content(),
                info.createdAt()
        );
    }
}
