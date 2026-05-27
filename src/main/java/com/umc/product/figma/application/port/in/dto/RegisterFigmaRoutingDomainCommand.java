package com.umc.product.figma.application.port.in.dto;

public record RegisterFigmaRoutingDomainCommand(
    String domainKey,
    String description,
    String discordWebhookUrl,
    boolean fallback
) {
}
