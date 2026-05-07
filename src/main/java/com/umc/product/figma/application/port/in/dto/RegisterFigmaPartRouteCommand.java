package com.umc.product.figma.application.port.in.dto;

public record RegisterFigmaPartRouteCommand(
    String fileKey,
    String pageName,
    String partKey,
    String discordRoleId,
    String discordWebhookUrl,
    boolean fallback
) {
}
