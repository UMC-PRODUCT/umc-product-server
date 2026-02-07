package com.umc.product.community.application.port.in.post.command;

import java.time.LocalDateTime;

public record CreateLightningCommand(
        String title,
        String content,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
}
