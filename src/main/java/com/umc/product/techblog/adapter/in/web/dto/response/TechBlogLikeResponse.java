package com.umc.product.techblog.adapter.in.web.dto.response;

import com.umc.product.techblog.application.port.in.query.dto.TechBlogLikeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테크 블로그 좋아요 응답")
public record TechBlogLikeResponse(
    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    boolean likedByMe,

    @Schema(description = "좋아요 수", example = "42")
    int likeCount
) {

    public static TechBlogLikeResponse from(TechBlogLikeInfo info) {
        return new TechBlogLikeResponse(info.likedByMe(), info.likeCount());
    }
}
