package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.PostSearchResult.MatchType;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;

public record PostSearchData(
        Long postId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
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
                region,
                anonymous,
                likeCount,
                createdAt,
                matchType
        );
    }
}
