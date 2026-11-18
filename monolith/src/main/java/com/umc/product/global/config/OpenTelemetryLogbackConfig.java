package com.umc.product.global.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.autoconfigure.logging.SdkLoggerProviderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.logs.LogLimits;

@Configuration
public class OpenTelemetryLogbackConfig implements InitializingBean {

    /**
     * Loki 는 OTLP 로그 속성을 structured metadata 로 저장하고, 한 줄의 structured metadata 가 64KB(기본값)를
     * 넘으면 그 줄을 통째로 버린다. 깊은 예외 스택 하나가 이 한도를 넘을 수 있어 속성 값 길이에 상한을 둔다.
     */
    public static final int MAX_LOG_ATTRIBUTE_LENGTH = 32_000;

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryLogbackConfig(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Bean
    public static SdkLoggerProviderBuilderCustomizer logAttributeLengthLimit() {
        return builder -> builder.setLogLimits(
            () -> LogLimits.builder().setMaxAttributeValueLength(MAX_LOG_ATTRIBUTE_LENGTH).build()
        );
    }

    @Override
    public void afterPropertiesSet() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }
}
