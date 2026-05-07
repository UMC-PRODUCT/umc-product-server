package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaPartRouteCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFigmaPartRouteRequest(
    @NotBlank @Size(max = 100) String fileKey,
    @NotBlank @Size(max = 255) String pageName,
    @NotBlank @Size(max = 50) String partKey,
    @NotBlank @Size(max = 50) String discordRoleId,
    @NotBlank String discordWebhookUrl,
    boolean fallback
) {
    public RegisterFigmaPartRouteCommand toCommand() {
        return new RegisterFigmaPartRouteCommand(
            fileKey, pageName, partKey, discordRoleId, discordWebhookUrl, fallback
        );
    }
}
