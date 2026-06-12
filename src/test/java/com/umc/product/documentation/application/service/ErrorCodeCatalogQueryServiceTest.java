package com.umc.product.documentation.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.documentation.adapter.in.web.dto.response.ErrorCodeCatalogItemResponse;
import com.umc.product.documentation.adapter.in.web.dto.response.ErrorCodeCatalogResponse;

@DisplayName("ErrorCodeCatalogQueryService")
class ErrorCodeCatalogQueryServiceTest {

    private final ErrorCodeCatalogQueryService service = new ErrorCodeCatalogQueryService(new ObjectMapper());

    @Test
    @DisplayName("생성된 ErrorCode v1 manifest를 classpath에서 읽는다")
    void 생성된_ErrorCode_v1_manifest를_classpath에서_읽는다() {
        ErrorCodeCatalogResponse catalog = service.getErrorCodeCatalog();

        assertThat(catalog.schemaVersion()).isEqualTo(1);
        assertThat(catalog.service()).isEqualTo("umc-product-server");
        assertThat(catalog.generatedAt()).isNull();
        assertThat(catalog.totalCount()).isEqualTo(catalog.items().size());
        assertThat(catalog.items()).isNotEmpty();
        assertThat(catalog.items()).allSatisfy(item -> {
            assertThat(item.owners()).isNotNull();
            assertThat(item.tags()).isNotNull();
            assertThat(item.source().enumName()).isNotBlank();
            assertThat(item.source().file()).isNotBlank();
            assertThat(item.source().line()).isPositive();
        });
    }

    @Test
    @DisplayName("ErrorCodeSpec optional metadata를 manifest에 반영한다")
    void ErrorCodeSpec_optional_metadata를_manifest에_반영한다() {
        ErrorCodeCatalogResponse catalog = service.getErrorCodeCatalog();

        ErrorCodeCatalogItemResponse item = catalog.items().stream()
            .filter(candidate -> "DOCS-0001".equals(candidate.code()))
            .findFirst()
            .orElseThrow();

        assertThat(item.description()).isEqualTo(
            "Generated ErrorCode catalog resource is missing, unreadable, or invalid."
        );
        assertThat(item.clientAction()).isEqualTo(
            "Retry after the server has regenerated and redeployed the catalog."
        );
        assertThat(item.retryable()).isTrue();
        assertThat(item.severity()).isEqualTo("ERROR");
        assertThat(item.owners()).containsExactly("server");
        assertThat(item.tags()).containsExactly("documentation", "backoffice");
    }
}
