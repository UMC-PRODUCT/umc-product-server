package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.AddFigmaRoutingMentionCommand;
import com.umc.product.figma.domain.enums.DiscordMentionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddFigmaRoutingMentionRequest(
    @NotBlank @Size(max = 50) String mentionId,
    @NotNull DiscordMentionType mentionType,
    @Size(max = 255) String displayLabel
) {
    public AddFigmaRoutingMentionCommand toCommand(Long domainId) {
        return new AddFigmaRoutingMentionCommand(domainId, mentionId, mentionType, displayLabel);
    }
}
