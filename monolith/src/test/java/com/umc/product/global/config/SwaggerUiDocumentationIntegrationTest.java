package com.umc.product.global.config;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestPropertySource;

import com.umc.product.support.IntegrationTestSupport;

@TestPropertySource(properties = "OPENAPI_ENABLE=true")
@DisplayName("OpenAPI를 켠 환경의 Swagger UI 문서 화면")
class SwaggerUiDocumentationIntegrationTest extends IntegrationTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {"/docs", "/docs/", "/docs/scalar.html"})
    @DisplayName("문서 진입 경로와 이전 Scalar 주소는 Swagger UI로 리다이렉트한다")
    void 문서_진입_경로는_Swagger_UI로_이동한다(String entryPath) throws Exception {
        // when & then
        mockMvc.perform(get(entryPath))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/docs/swagger-ui.html"));
    }

    @Test
    @DisplayName("Swagger UI 주소는 /docs 아래의 화면으로 리다이렉트한다")
    void Swagger_UI_주소는_docs_아래_화면으로_이동한다() throws Exception {
        // when & then
        mockMvc.perform(get("/docs/swagger-ui.html"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/docs/swagger-ui/index.html"));
    }

    @Test
    @DisplayName("Swagger UI 화면은 /docs 아래에서 HTML로 제공한다")
    void Swagger_UI_화면을_제공한다() throws Exception {
        // when & then
        mockMvc.perform(get("/docs/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"))
            .andExpect(content().string(Matchers.containsString("swagger-ui-bundle.js")));
    }

    @Test
    @DisplayName("Swagger UI 화면은 외부 예제 문서가 아니라 /docs-json 아래의 설정을 읽는다")
    void Swagger_UI_화면은_docs_json_설정을_읽는다() throws Exception {
        // when & then
        mockMvc.perform(get("/docs/swagger-ui/swagger-initializer.js"))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("/docs-json/swagger-config")))
            .andExpect(content().string(Matchers.not(Matchers.containsString("petstore.swagger.io"))));
    }

    @Test
    @DisplayName("Swagger UI 설정은 /docs-json 문서를 가리키고 그 문서를 제공한다")
    void Swagger_UI_설정은_docs_json_문서를_가리킨다() throws Exception {
        // when & then
        mockMvc.perform(get("/docs-json/swagger-config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("/docs-json"));
        mockMvc.perform(get("/docs-json"))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/swagger-ui.html",
        "/swagger-ui/index.html",
        "/v3/api-docs",
        "/v3/api-docs/swagger-config",
        "/swagger-resources",
        "/webjars/swagger-ui/index.html"
    })
    @DisplayName("Swagger UI를 켜도 인그레스 Basic Auth 밖의 기본 경로는 인증된 요청에도 막는다")
    void 기본_Swagger_경로는_계속_차단한다(String blockedPath) throws Exception {
        // given
        String token = "swagger-ui-block-token";
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of("USER"));
        given(jwtTokenProvider.getClientTypeFromAccessToken(token)).willReturn(null);

        // when & then
        mockMvc.perform(get(blockedPath)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }
}
