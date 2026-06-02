package com.umc.product.techblog.application.port.in.query.dto;

import com.umc.product.techblog.domain.TechBlogCommentDeletionType;
import java.time.Instant;
import java.util.List;

public record TechBlogCommentInfo(
    Long id,
    TechBlogAuthorInfo author,
    String content,
    Instant createdAt,
    boolean likedByMe,
    int likeCount,
    TechBlogCommentDeletionType deletionType,
    boolean canReply,
    List<TechBlogCommentInfo> replies
) {
}
