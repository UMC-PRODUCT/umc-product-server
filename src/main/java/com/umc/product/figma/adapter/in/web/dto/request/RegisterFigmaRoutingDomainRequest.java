package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaRoutingDomainCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFigmaRoutingDomainRequest(
    @NotBlank @Size(max = 100) String domainKey,
    @Size(max = 500) String description,
    @NotBlank String discordWebhookUrl,
    boolean fallback
) {
    public RegisterFigmaRoutingDomainCommand toCommand() {
        return new RegisterFigmaRoutingDomainCommand(domainKey, description, discordWebhookUrl, fallback);
    }
}
