package com.umc.product.figma.adapter.in.web.dto.response;

public record FigmaOAuthAuthorizeResponse(
    String authorizeUrl,
    String state
) {
}
