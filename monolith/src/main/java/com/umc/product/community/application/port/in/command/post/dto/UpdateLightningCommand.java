package com.umc.product.community.application.port.in.command.post.dto;

import java.time.Instant;

public record UpdateLightningCommand(
    Long postId,
    String title,
    String content,
    Instant meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl
) {
}
