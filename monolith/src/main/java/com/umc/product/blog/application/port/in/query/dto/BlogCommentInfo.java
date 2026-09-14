package com.umc.product.blog.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.blog.domain.BlogCommentDeletionType;

public record BlogCommentInfo(
    Long id,
    BlogAuthorInfo author,
    String content,
    Instant createdAt,
    boolean likedByMe,
    int likeCount,
    BlogCommentDeletionType deletionType,
    boolean canReply,
    boolean canEdit,
    boolean canDelete,
    List<BlogCommentInfo> replies
) {
}
