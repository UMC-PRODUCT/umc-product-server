package com.umc.product.documentation.adapter.in.web.dto.response;

public record ErrorCodeCatalogSourceResponse(
    String enumName,
    String file,
    int line
) {
}
