package com.umc.product.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;

/**
 * 운영과 같은 배선(SanitizingAppender → OpenTelemetryAppender)으로 Loki 에 실리는 로그 레코드를 검증한다.
 */
class SanitizingAppenderOtlpExportTest {

    private static final String PROBE_EMAIL = "log-probe@example.invalid";
    private static final AttributeKey<String> EXCEPTION_MESSAGE = AttributeKey.stringKey("exception.message");
    private static final AttributeKey<String> EXCEPTION_STACKTRACE = AttributeKey.stringKey("exception.stacktrace");

    private final InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    private LoggerContext context;
    private OpenTelemetrySdk openTelemetry;
    private Logger logger;

    @BeforeEach
    void setUp() {
        context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());

        openTelemetry = OpenTelemetrySdk.builder()
            .setLoggerProvider(SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
                .build())
            .build();

        OpenTelemetryAppender otel = new OpenTelemetryAppender();
        otel.setContext(context);
        otel.setName("OTEL");
        otel.setOpenTelemetry(openTelemetry);
        otel.start();

        SanitizingAppender sanitizing = new SanitizingAppender();
        sanitizing.setContext(context);
        sanitizing.setName("SANITIZED");
        sanitizing.addAppender(otel);
        sanitizing.start();

        logger = context.getLogger("otlp-export-test");
        logger.setAdditive(false);
        logger.setLevel(Level.ERROR);
        logger.addAppender(sanitizing);
    }

    @AfterEach
    void tearDown() {
        context.stop();
        openTelemetry.close();
    }

    @Test
    @DisplayName("예외 스택이 원래 예외 클래스명과 함께 정제된 채로 OTLP 로그에 실린다")
    void 예외_스택_OTLP_전달() {
        IllegalStateException error = new IllegalStateException("email=" + PROBE_EMAIL);

        logger.error("작업 실패", error);

        Attributes attributes = exporter.getFinishedLogRecordItems().getFirst().getAttributes();
        assertThat(attributes.get(EXCEPTION_MESSAGE)).isEqualTo("email=[REDACTED]");
        assertThat(attributes.get(EXCEPTION_STACKTRACE))
            .startsWith("java.lang.IllegalStateException: email=[REDACTED]")
            .contains("at " + getClass().getName() + ".")
            .doesNotContain(PROBE_EMAIL);
    }

    @Test
    @DisplayName("원인 예외도 원래 클래스명과 정제된 메시지로 OTLP 스택에 남는다")
    void 원인_예외_OTLP_전달() {
        RuntimeException error = new RuntimeException(
            "저장 실패",
            new IllegalArgumentException("duplicate " + PROBE_EMAIL)
        );

        logger.error("작업 실패", error);

        String stacktrace = exporter.getFinishedLogRecordItems().getFirst().getAttributes().get(EXCEPTION_STACKTRACE);
        assertThat(stacktrace)
            .contains("Caused by: java.lang.IllegalArgumentException: duplicate [REDACTED]")
            .doesNotContain(PROBE_EMAIL);
    }
}
