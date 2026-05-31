package com.umc.product.figma.application.port.in.dto;

import com.umc.product.figma.domain.enums.DiscordMentionType;

public record FigmaRoutingDomainMentionInfo(
    Long id,
    Long domainId,
    String mentionId,
    DiscordMentionType mentionType,
    String displayLabel
) {
}
