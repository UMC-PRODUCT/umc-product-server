package com.umc.product.notification.adapter.in.scheduler;

import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.domain.WebhookPlatform;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.metrics-report.enabled", havingValue = "true")
public class MetricsReportScheduler {

    private static final List<WebhookPlatform> PLATFORMS = List.of(WebhookPlatform.TELEGRAM, WebhookPlatform.DISCORD);

    private final MeterRegistry meterRegistry;
    private final SendWebhookAlarmUseCase sendWebhookAlarmUseCase;
    private final Environment environment;

    @Scheduled(fixedRateString = "${app.metrics-report.interval-ms:3600000}")
    public void reportMetrics() {
        log.debug("메트릭 보고 스케줄 실행");

        String profile = getCapitalizedProfile();
        String content = buildMetricsReport();

        SendWebhookAlarmCommand command = SendWebhookAlarmCommand.builder()
            .platforms(PLATFORMS)
            .title("[" + profile + "] 서버 메트릭 보고")
            .content(content)
            .build();

        sendWebhookAlarmUseCase.send(command);
    }

    private String buildMetricsReport() {
        StringBuilder sb = new StringBuilder();

        // CPU
        double processCpu = getGaugeValue("process.cpu.usage") * 100;
        double systemCpu = getGaugeValue("system.cpu.usage") * 100;
        sb.append("[CPU]\n");
        sb.append(String.format("  Process: %.1f%%\n", processCpu));
        sb.append(String.format("  System: %.1f%%\n", systemCpu));

        // Memory (heap)
        double memoryUsed = getGaugeValue("jvm.memory.used", "area", "heap") / (1024 * 1024);
        double memoryMax = getGaugeValue("jvm.memory.max", "area", "heap") / (1024 * 1024);
        sb.append("\n[Memory - Heap]\n");
        sb.append(String.format("  Used: %.0f MB\n", memoryUsed));
        sb.append(String.format("  Max: %.0f MB\n", memoryMax));
        if (memoryMax > 0) {
            sb.append(String.format("  Usage: %.1f%%\n", (memoryUsed / memoryMax) * 100));
        }

        // Threads
        double liveThreads = getGaugeValue("jvm.threads.live");
        sb.append("\n[Threads]\n");
        sb.append(String.format("  Live: %.0f\n", liveThreads));

        // HTTP Requests
        double httpRequestCount = getTimerCount("http.server.requests");
        double httpRequestTotalTime = getTimerTotalTime("http.server.requests");
        sb.append("\n[HTTP Requests]\n");
        sb.append(String.format("  Total Count: %.0f\n", httpRequestCount));
        if (httpRequestCount > 0) {
            sb.append(String.format("  Avg Response: %.1f ms\n", (httpRequestTotalTime / httpRequestCount) * 1000));
        }

        // DB Connections
        double activeConnections = getGaugeValue("hikaricp.connections.active");
        double pendingConnections = getGaugeValue("hikaricp.connections.pending");
        sb.append("\n[DB Connections]\n");
        sb.append(String.format("  Active: %.0f\n", activeConnections));
        sb.append(String.format("  Pending: %.0f", pendingConnections));

        return sb.toString();
    }

    private double getGaugeValue(String name) {
        var gauge = meterRegistry.find(name).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }

    private double getGaugeValue(String name, String tagKey, String tagValue) {
        var gauge = meterRegistry.find(name).tag(tagKey, tagValue).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }

    private double getTimerCount(String name) {
        Timer timer = meterRegistry.find(name).timer();
        return timer != null ? timer.count() : 0.0;
    }

    private double getTimerTotalTime(String name) {
        Timer timer = meterRegistry.find(name).timer();
        return timer != null ? timer.totalTime(TimeUnit.SECONDS) : 0.0;
    }

    private String getCapitalizedProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "Default";
        }
        return String.join(", ", Arrays.stream(profiles)
            .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
            .toList());
    }
}
