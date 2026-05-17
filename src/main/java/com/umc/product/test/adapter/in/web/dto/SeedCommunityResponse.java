package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedCommunityResult;
import java.util.List;

public record SeedCommunityResponse(
    Long gisuId,
    List<Long> createdPostIds,
    List<Long> createdCommentIds,
    List<Long> createdTrophyIds,
    int postFailed,
    int commentFailed,
    int trophyFailed,
    boolean skipped,
    String reason
) {

    public static SeedCommunityResponse from(SeedCommunityResult result) {
        return new SeedCommunityResponse(
            result.gisuId(),
            result.createdPostIds(),
            result.createdCommentIds(),
            result.createdTrophyIds(),
            result.postFailed(),
            result.commentFailed(),
            result.trophyFailed(),
            result.skipped(),
            result.reason()
        );
    }
}
