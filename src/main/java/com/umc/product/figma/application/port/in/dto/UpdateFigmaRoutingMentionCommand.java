package com.umc.product.figma.application.port.in.dto;

public record UpdateFigmaRoutingMentionCommand(
    Long id,
    String mentionId,
    String displayLabel
) {
}
