package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.UpdateFigmaRoutingMentionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFigmaRoutingMentionRequest(
    @NotBlank @Size(max = 50) String mentionId,
    @Size(max = 255) String displayLabel
) {
    public UpdateFigmaRoutingMentionCommand toCommand(Long id) {
        return new UpdateFigmaRoutingMentionCommand(id, mentionId, displayLabel);
    }
}
