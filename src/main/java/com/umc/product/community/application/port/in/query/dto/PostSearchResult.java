package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import lombok.Builder;

/**
 * {@link com.umc.product.community.application.port.out.dto.PostSearchData} 에서 만들어졌습니다.
 */
@Builder
public record PostSearchResult(
    Long postId,
    String title,
    String contentPreview,
    Category category,
    int likeCount,
    Instant createdAt,
    MatchType matchType
) {

    public enum MatchType {
        TITLE_START,
        TITLE_CONTAIN,
        CONTENT
    }

    public static PostSearchResult of(
        Long postId,
        String title,
        String content,
        Category category,
        int likeCount,
        Instant createdAt,
        MatchType matchType
    ) {
        return PostSearchResult.builder()
            .postId(postId)
            .title(title)
            .contentPreview(truncateContent(content, 100))
            .category(category)
            .likeCount(likeCount)
            .createdAt(createdAt)
            .matchType(matchType)
            .build();
    }

    private static String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
