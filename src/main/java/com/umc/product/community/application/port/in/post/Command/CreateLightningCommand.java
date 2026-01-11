package com.umc.product.community.application.port.in.post.Command;

import java.time.LocalDateTime;

public record CreateLightningCommand(
        String title,
        String content,
        String region,
        boolean anonymous,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
}
