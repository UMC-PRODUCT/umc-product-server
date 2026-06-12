package com.umc.product.documentation.adapter.in.web.dto.response;

import java.util.List;

public record ErrorCodeCatalogResponse(
    int schemaVersion,
    String service,
    String generatedAt,
    int totalCount,
    List<ErrorCodeCatalogItemResponse> items
) {
}
