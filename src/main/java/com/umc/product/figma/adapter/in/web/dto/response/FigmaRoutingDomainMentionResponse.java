package com.umc.product.figma.adapter.in.web.dto.response;

import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainMentionInfo;
import com.umc.product.figma.domain.enums.DiscordMentionType;

public record FigmaRoutingDomainMentionResponse(
    Long id,
    Long domainId,
    String mentionId,
    DiscordMentionType mentionType,
    String displayLabel
) {

    public static FigmaRoutingDomainMentionResponse from(FigmaRoutingDomainMentionInfo info) {
        return new FigmaRoutingDomainMentionResponse(
            info.id(),
            info.domainId(),
            info.mentionId(),
            info.mentionType(),
            info.displayLabel()
        );
    }
}
