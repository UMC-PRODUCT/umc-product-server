package com.umc.product.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.constant.SwaggerTag;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private final String accessToken = "Access Token";
    private final String refreshToken = "Refresh Token";


    @Bean
    public OpenAPI umcProductApi() {

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(tags())  // Enum으로 관리하는 Tags
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
                        .description("Local")
        );
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // Spring이 관리하는 ObjectMapper를 Swagger에 전달
        return new ModelResolver(objectMapper);
    }

    /**
     * Enum으로 관리하는 Tags를 OpenAPI Tag로 변환
     */
    private List<Tag> tags() {
        return Arrays.stream(SwaggerTag.values())
                .sorted(Comparator.comparingInt(SwaggerTag::getOrder))  // order 순으로 정렬
                .map(SwaggerTag::toTag)
                .collect(Collectors.toList());
    }

    private Components securityComponents() {
        return new Components()
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
                );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList(accessToken)
                .addList(refreshToken);
    }
}
