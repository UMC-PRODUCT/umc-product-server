package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

class SecurityPathConfigTest {

    @Test
    @DisplayName("문서 공개 경로는 Scalar와 문서 카탈로그에 필요한 경로만 포함한다")
    void documentationPathsExposeScalarAndCatalogOnly() {
        assertThat(SecurityPathConfig.DOCUMENTATION_PATHS)
            .contains(
                "/docs",
                "/docs/",
                "/docs/**",
                "/docs-json",
                "/docs-json/**",
                "/webjars/markdown-it/**",
                "/umc-logo.svg"
            )
            .doesNotContain(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/webjars/swagger-ui/**"
            );
    }

    @Test
    @DisplayName("Swagger 경로는 인증 여부와 무관하게 차단 대상이다")
    void swaggerPathsAreBlockedExplicitly() {
        assertThat(SecurityPathConfig.SWAGGER_BLOCKED_PATHS)
            .contains(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/swagger-ui/**"
            )
            .doesNotContain(
                "/docs",
                "/docs/**",
                "/docs-json",
                "/docs-json/**",
                "/webjars/markdown-it/**"
            );
    }

    @Test
    @DisplayName("Springdoc은 Swagger UI를 끄고 Scalar가 사용할 OpenAPI JSON만 제공한다")
    void springdocDisablesSwaggerUiAndKeepsOpenApiJson() {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new ClassPathResource("application.yml"));

        Properties properties = yamlPropertiesFactoryBean.getObject();

        assertThat(properties)
            .containsEntry("springdoc.swagger-ui.enabled", Boolean.FALSE)
            .containsEntry("springdoc.api-docs.path", "/docs-json")
            .containsEntry("springdoc.api-docs.enabled", "${OPENAPI_ENABLE:${SWAGGER_ENABLE:false}}");
    }

    @Test
    @DisplayName("maintenance allowlist는 SSO 공개 진입 경로를 명시적으로 포함한다")
    void maintenanceAllowlistContainsSsoPublicPaths() {
        assertThat(SecurityPathConfig.MAINTENANCE_ALWAYS_ALLOW_PATHS)
            .contains(
                "/api/v1/oauth/**",
                "/api/v1/auth/browser-login/**"
            );
    }
}
