package com.umc.product.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        String accessToken = "Access Token";
        String refreshToken = "Refresh Token";

        return new OpenAPI()
                .info(new Info()
                        .title("UMC PRODUCT TEAM API")
                        .version("0.1.0")
                        .description("UMC Product Team API"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(accessToken)
                        .addList(refreshToken)
                )
                .components(new Components()
                        .addSecuritySchemes(accessToken,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("발급받은 Access Token을 입력해주세요.")
                        )
                        .addSecuritySchemes(refreshToken,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Refresh Token을 입력해주세요.")
                        )
                );
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // Spring이 관리하는 ObjectMapper를 Swagger에 전달
        return new ModelResolver(objectMapper);
    }
}
