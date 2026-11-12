package com.umc.product.global.observability.chat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatWebSocketMetrics {

    private final MeterRegistry registry;

    public Runnable decorate(String direction, Runnable task) {
        long submittedAt = registry.config().clock().monotonicTime();
        return () -> {
            long startedAt = registry.config().clock().monotonicTime();
            Timer.builder("umc.chat.websocket.queue.wait")
                .description("WebSocket 실행 작업의 제출부터 실행 시작까지, 모든 프레임 포함")
                .tag("direction", direction)
                .serviceLevelObjectives(Duration.ofMillis(1), Duration.ofMillis(5), Duration.ofMillis(10),
                    Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(500),
                    Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(30))
                .register(registry)
                .record(startedAt - submittedAt, TimeUnit.NANOSECONDS);
            task.run();
        };
    }
}
