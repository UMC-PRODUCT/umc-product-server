package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.community.domain.enums.Category;
import java.time.Instant;

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
        return new PostSearchResult(
            postId,
            title,
            truncateContent(content, 100),
            category,
            likeCount,
            createdAt,
            matchType
        );
    }

    private static String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
