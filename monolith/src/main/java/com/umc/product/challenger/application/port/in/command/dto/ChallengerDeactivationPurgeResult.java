package com.umc.product.challenger.application.port.in.command.dto;

public record ChallengerDeactivationPurgeResult(
    int deletedCount,
    int failedCount
) {
}
