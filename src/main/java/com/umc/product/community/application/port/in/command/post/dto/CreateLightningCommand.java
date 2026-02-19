package com.umc.product.community.application.port.in.command.post.dto;

import java.time.LocalDateTime;

public record CreateLightningCommand(
    String title,
    String content,
    LocalDateTime meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl,
    Long authorChallengerId
) {
}
