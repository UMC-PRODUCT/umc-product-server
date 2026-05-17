package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedCommunityCommand;
import jakarta.validation.constraints.PositiveOrZero;

public record SeedCommunityRequest(
    Long gisuId,
    @PositiveOrZero
    int postCount,
    @PositiveOrZero
    int commentsPerPost,
    @PositiveOrZero
    int trophyCount
) {

    public SeedCommunityCommand toCommand() {
        return new SeedCommunityCommand(gisuId, postCount, commentsPerPost, trophyCount);
    }
}
