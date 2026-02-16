package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.post.CommentInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "댓글 응답")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "게시글 ID", example = "5")
        Long postId,

        @Schema(description = "작성자 챌린저 ID", example = "10")
        Long challengerId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String challengerName,

        @Schema(description = "작성자 프로필 이미지", example = "https://example.com/profile.jpg")
        String challengerProfileImage,

        @Schema(description = "작성자 파트", example = "SPRINGBOOT")
        ChallengerPart challengerPart,

        @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
        String content,

        @Schema(description = "작성일시", example = "2026-02-13T10:30:00Z")
        Instant createdAt,

        @Schema(description = "본인 작성 댓글 여부", example = "true")
        boolean isAuthor
) {
    public static CommentResponse from(CommentInfo info) {
        return new CommentResponse(
                info.commentId(),
                info.postId(),
                info.challengerId(),
                info.challengerName(),
                info.challengerProfileImage(),
                info.challengerPart(),
                info.content(),
                info.createdAt(),
                info.isAuthor()
        );
    }
}
