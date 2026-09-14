package com.umc.product.blog.application.port.in.query.dto;

public record BlogLikeInfo(
    boolean likedByMe,
    int likeCount
) {
}
