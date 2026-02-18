package com.umc.product.community.application.port.out.dto;

import com.umc.product.community.application.port.in.query.dto.PostSearchResult;
import com.umc.product.community.application.port.in.query.dto.PostSearchResult.MatchType;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;

public record PostSearchData(
    Long postId,
    String title,
    String content,
    Category category,
    int likeCount,
    Instant createdAt,
    MatchType matchType,
    int relevanceScore
) {

    public PostSearchResult toResult() {
        return PostSearchResult.of(
            postId,
            title,
            content,
            category,
            likeCount,
            createdAt,
            matchType
        );
    }
}
