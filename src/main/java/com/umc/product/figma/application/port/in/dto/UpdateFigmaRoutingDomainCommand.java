package com.umc.product.figma.application.port.in.dto;

public record UpdateFigmaRoutingDomainCommand(
    Long domainId,
    String description,
    String discordWebhookUrl,
    boolean fallback
) {
}
