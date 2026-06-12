package com.umc.product.documentation.adapter.in.web.dto.response;

import java.util.List;

public record ErrorCodeCatalogItemResponse(
    int sequence,
    String domain,
    String code,
    String name,
    int httpStatus,
    String httpStatusName,
    String message,
    String description,
    String clientAction,
    Boolean retryable,
    String severity,
    boolean deprecated,
    String replacementCode,
    List<String> owners,
    List<String> tags,
    ErrorCodeCatalogSourceResponse source
) {
}
