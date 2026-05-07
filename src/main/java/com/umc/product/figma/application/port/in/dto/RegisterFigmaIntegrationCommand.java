package com.umc.product.figma.application.port.in.dto;

public record RegisterFigmaIntegrationCommand(
    Long ownerMemberId,
    String authorizationCode
) {
}
