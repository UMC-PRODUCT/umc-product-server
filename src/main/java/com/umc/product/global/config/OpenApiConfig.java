package com.umc.product.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final String accessToken = "Access Token";

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI umcProductApi() {

        return new OpenAPI()
            .info(apiInfo())
            .servers(servers())
            .components(securityComponents())
            .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
            .title("UMC PRODUCT TEAM API")
            .version("0.1.0")
            .description("UMC Product Team API");
    }

    private List<Server> servers() {
        return List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Local"),
            new Server()
                .url("https://dev.api.umc.it.kr")
                .description("Development"),
            new Server()
                .url("https://api.umc.it.kr")
                .description("Production")
        );
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // Spring이 관리하는 ObjectMapper를 Swagger에 전달
        return new ModelResolver(objectMapper);
    }

    private Components securityComponents() {
        return new Components()
            .addSecuritySchemes(accessToken,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("발급받은 Access Token을 입력해주세요.")
            );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
            .addList(accessToken);
    }
}
