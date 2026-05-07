package com.umc.product.figma.application.port.out.dto;

public record DiscordMentionMessage(
    String webhookUrl,
    String roleId,
    String content
) {
}
