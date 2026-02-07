package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.query.PostSearchResult;
import com.umc.product.community.application.port.in.post.query.PostSearchResult.MatchType;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;

public record PostSearchResponse(
        Long postId,
        String title,
        String contentPreview,
        Category category,
        int likeCount,
        Instant createdAt,
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
