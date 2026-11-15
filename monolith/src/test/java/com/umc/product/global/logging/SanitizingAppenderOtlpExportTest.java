package com.umc.product.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.global.config.OpenTelemetryLogbackConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;

class SanitizingAppenderOtlpExportTest {

    private static final String PROBE_EMAIL = "log-probe@example.invalid";
    private static final AttributeKey<String> EXCEPTION_TYPE = AttributeKey.stringKey("exception.type");
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

        SdkLoggerProviderBuilder loggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
        OpenTelemetryLogbackConfig.logAttributeLengthLimit().customize(loggerProvider);
        openTelemetry = OpenTelemetrySdk.builder().setLoggerProvider(loggerProvider.build()).build();

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
    @DisplayName("OTLP 스택에는 원래 클래스명과 정제된 메시지가 남고 exception.type 은 사본 클래스명이 된다")
    void 예외_스택_OTLP_전달() {
        IllegalStateException error = new IllegalStateException("email=" + PROBE_EMAIL);

        logger.error("작업 실패", error);

        Attributes attributes = exporter.getFinishedLogRecordItems().getFirst().getAttributes();
        assertThat(attributes.get(EXCEPTION_TYPE)).isEqualTo(SanitizedThrowable.class.getName());
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

    @Test
    @DisplayName("순환 원인과 suppressed 예외의 민감값도 OTLP 스택에서 정제된다")
    void 순환_suppressed_OTLP_정제() {
        RuntimeException outer = new RuntimeException("결제 실패: " + PROBE_EMAIL);
        RuntimeException inner = new RuntimeException("커넥션 실패");
        outer.initCause(inner);
        inner.initCause(outer);
        outer.addSuppressed(new IllegalStateException("close 실패: " + PROBE_EMAIL));

        logger.error("작업 실패", outer);

        String stacktrace = exporter.getFinishedLogRecordItems().getFirst().getAttributes().get(EXCEPTION_STACKTRACE);
        assertThat(stacktrace)
            .contains("[CIRCULAR REFERENCE: java.lang.RuntimeException: 결제 실패: [REDACTED]]")
            .contains("Suppressed: java.lang.IllegalStateException: close 실패: [REDACTED]")
            .doesNotContain(PROBE_EMAIL);
    }

    @Test
    @DisplayName("깊은 예외 스택은 Loki 한도 안으로 잘려 로그 레코드가 남는다")
    void 깊은_스택_길이_제한() {
        RuntimeException error = deepException(3_000);

        logger.error("작업 실패", error);

        String stacktrace = exporter.getFinishedLogRecordItems().getFirst().getAttributes().get(EXCEPTION_STACKTRACE);
        assertThat(stacktrace)
            .startsWith("java.lang.RuntimeException: 깊은 실패")
            .hasSizeLessThanOrEqualTo(OpenTelemetryLogbackConfig.MAX_LOG_ATTRIBUTE_LENGTH);
    }

    private static RuntimeException deepException(int depth) {
        return depth == 0 ? new RuntimeException("깊은 실패") : deepException(depth - 1);
    }
}
