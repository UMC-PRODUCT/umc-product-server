package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "게시글 응답")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "스터디원 모집합니다")
        String title,

        @Schema(description = "내용", example = "Spring Boot 스터디원 모집합니다.")
        String content,

        @Schema(description = "카테고리", example = "FREE")
        Category category,

        @Schema(description = "작성자 ID", example = "123")
        Long authorId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성일시", example = "2026-02-13T10:30:00Z")
        Instant createdAt,

        @Schema(description = "좋아요 수", example = "42")
        int likeCount,

        @Schema(description = "좋아요 여부", example = "true")
        boolean isLiked,

        @Schema(description = "번개 정보 (번개글인 경우)")
        LightningInfoResponse lightningInfo
) {
    public static PostResponse from(PostInfo info) {
        LightningInfoResponse lightningInfoResponse = null;

        if (info.category() == Category.LIGHTNING) {
            lightningInfoResponse = new LightningInfoResponse(
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants(),
                    info.openChatUrl()
            );
        }

        return new PostResponse(
                info.postId(),
                info.title(),
                info.content(),
                info.category(),
                info.authorId(),
                info.authorName(),
                info.createdAt(),
                info.likeCount(),
                info.isLiked(),
                lightningInfoResponse
        );
    }

    @Schema(description = "번개 정보")
    public record LightningInfoResponse(
            @Schema(description = "모임 시간", example = "2026-03-16T18:00:00")
            LocalDateTime meetAt,

            @Schema(description = "모임 장소", example = "강남역 2번 출구")
            String location,

            @Schema(description = "최대 참가자 수", example = "5")
            Integer maxParticipants,

            @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/o/sxxxxxx")
            String openChatUrl
    ) {
    }
}
