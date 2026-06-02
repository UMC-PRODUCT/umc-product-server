package com.umc.product.techblog.application.port.in.query.dto;

public record TechBlogLikeInfo(
    boolean likedByMe,
    int likeCount
) {
}
