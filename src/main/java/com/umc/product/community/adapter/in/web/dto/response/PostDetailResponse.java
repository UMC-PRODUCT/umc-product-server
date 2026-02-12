package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.post.query.PostDetailInfo;
import com.umc.product.community.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "게시글 상세 응답")
public record PostDetailResponse(
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

        @Schema(description = "번개 정보 (번개글인 경우)")
        LightningInfoResponse lightningInfo,

        @Schema(description = "댓글 수", example = "5")
        int commentCount,

        @Schema(description = "작성 시간", example = "2026-02-11T10:30:00Z")
        String writeTime,

        @Schema(description = "좋아요 수", example = "10")
        int likeCount,

        @Schema(description = "작성자 파트", example = "SPRINGBOOT")
        ChallengerPart userPart,

        @Schema(description = "좋아요 클릭 여부", example = "true")
        boolean isLiked,

        @Schema(description = "스크랩 클릭 여부", example = "false")
        boolean isScrapped,

        @Schema(description = "스크랩 수", example = "3")
        int scrapCount
) {
    public static PostDetailResponse from(PostDetailInfo info) {
        LightningInfoResponse lightningInfoResponse = null;

        if (info.category() == Category.LIGHTNING) {
            lightningInfoResponse = new LightningInfoResponse(
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants(),
                    info.openChatUrl()
            );
        }

        String writeTime = info.createdAt() != null ? info.createdAt().toString() : null;

        return new PostDetailResponse(
                info.postId(),
                info.title(),
                info.content(),
                info.category(),
                info.authorId(),
                info.authorName(),
                lightningInfoResponse,
                info.commentCount(),
                writeTime,
                info.likeCount(),
                info.userPart(),
                info.isLiked(),
                info.isScrapped(),
                info.scrapCount()
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
