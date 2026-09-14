package com.umc.product.global.config;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .featuresToEnable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO 8601 형식으로 직렬화
    }
}
