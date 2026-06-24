package com.umc.product.blog.adapter.in.web.dto.response;

import com.umc.product.blog.application.port.in.query.dto.BlogLikeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "블로그 좋아요 응답")
public record BlogLikeResponse(
    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    boolean likedByMe,

    @Schema(description = "좋아요 수", example = "42")
    int likeCount
) {

    public static BlogLikeResponse from(BlogLikeInfo info) {
        return new BlogLikeResponse(info.likedByMe(), info.likeCount());
    }
}
