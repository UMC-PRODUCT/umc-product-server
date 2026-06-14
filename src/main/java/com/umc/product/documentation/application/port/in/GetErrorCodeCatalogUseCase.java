package com.umc.product.documentation.application.port.in;

import com.umc.product.documentation.adapter.in.web.dto.response.ErrorCodeCatalogResponse;

public interface GetErrorCodeCatalogUseCase {

    ErrorCodeCatalogResponse getErrorCodeCatalog();
}
