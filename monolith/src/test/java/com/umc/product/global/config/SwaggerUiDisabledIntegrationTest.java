package com.umc.product.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestPropertySource;

import com.umc.product.support.IntegrationTestSupport;

@TestPropertySource(properties = "OPENAPI_ENABLE=false")
@DisplayName("OpenAPI를 끈 환경의 Swagger UI 문서 화면")
class SwaggerUiDisabledIntegrationTest extends IntegrationTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {
        "/docs/swagger-ui.html",
        "/docs/swagger-ui/index.html",
        "/docs-json/swagger-config",
        "/docs-json"
    })
    @DisplayName("OpenAPI를 끄면 Swagger UI 화면과 문서를 제공하지 않는다")
    void OpenAPI를_끄면_문서를_제공하지_않는다(String documentationPath) throws Exception {
        // when & then
        mockMvc.perform(get(documentationPath))
            .andExpect(status().isNotFound());
    }
}
