package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.UpdateFigmaRoutingDomainCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFigmaRoutingDomainRequest(
    @Size(max = 500) String description,
    @NotBlank String discordWebhookUrl,
    boolean fallback
) {
    public UpdateFigmaRoutingDomainCommand toCommand(Long domainId) {
        return new UpdateFigmaRoutingDomainCommand(domainId, description, discordWebhookUrl, fallback);
    }
}
