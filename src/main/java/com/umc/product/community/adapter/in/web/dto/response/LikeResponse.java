package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.command.comment.ToggleCommentLikeUseCase;
import com.umc.product.community.application.port.in.command.post.TogglePostLikeUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 토글 응답")
public record LikeResponse(
    @Schema(description = "좋아요 상태 (true: 좋아요, false: 좋아요 취소)", example = "true")
    boolean liked,

    @Schema(description = "현재 좋아요 수", example = "42")
    int likeCount
) {
    public static LikeResponse from(TogglePostLikeUseCase.LikeResult result) {
        return new LikeResponse(result.liked(), result.likeCount());
    }

    public static LikeResponse from(ToggleCommentLikeUseCase.LikeResult result) {
        return new LikeResponse(result.liked(), result.likeCount());
    }
}
