package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.PostSearchResult.MatchType;
import com.umc.product.community.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "게시글 검색 결과 응답")
public record PostSearchResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "제목", example = "Spring Boot 스터디원 모집")
        String title,

        @Schema(description = "내용 미리보기 (100자)", example = "스프링 부트를 공부할 팀원을 모집합니다...")
        String contentPreview,

        @Schema(description = "카테고리", example = "FREE")
        Category category,

        @Schema(description = "좋아요 수", example = "10")
        int likeCount,

        @Schema(description = "작성일시", example = "2026-02-13T10:30:00Z")
        Instant createdAt,

        @Schema(description = "매칭 타입 (TITLE_START: 제목 시작, TITLE_CONTAINS: 제목 포함, CONTENT_CONTAINS: 본문 포함)", example = "TITLE_START")
        MatchType matchType
) {

    public static PostSearchResponse from(PostSearchResult result) {
        return new PostSearchResponse(
                result.postId(),
                result.title(),
                result.contentPreview(),
                result.category(),
                result.likeCount(),
                result.createdAt(),
                result.matchType()
        );
    }
}
