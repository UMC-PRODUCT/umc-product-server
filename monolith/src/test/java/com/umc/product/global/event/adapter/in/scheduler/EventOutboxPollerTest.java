package com.umc.product.global.event.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.config.OutboxWorkerConfig;
import com.umc.product.global.event.application.service.EventOutboxRelayMetrics;
import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("Outbox 그룹별 폴링 연결")
class EventOutboxPollerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(OutboxRoutingConfig.class, OutboxWorkerConfig.class, EventOutboxPoller.class)
        .withBean(EventOutboxRelayMetrics.class, () -> new EventOutboxRelayMetrics(new SimpleMeterRegistry()))
        .withBean(EventOutboxRelayService.class, () -> mock(EventOutboxRelayService.class));

    @Test
    @DisplayName("그룹별 동시성 설정을 executor 크기와 Worker 슬롯에 함께 적용한다")
    void 동시성_설정_연결() {
        runner.withPropertyValues(
            "app.event-outbox.workers.realtime.concurrency=2",
            "app.event-outbox.workers.background.concurrency=3"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            var realtime = context.getBean("realtimeOutboxExecutor", ThreadPoolTaskExecutor.class);
            var background = context.getBean("backgroundOutboxExecutor", ThreadPoolTaskExecutor.class);
            assertThat(realtime.getCorePoolSize()).isEqualTo(2);
            assertThat(realtime.getMaxPoolSize()).isEqualTo(2);
            assertThat(realtime.getQueueCapacity()).isEqualTo(2);
            assertThat(background.getCorePoolSize()).isEqualTo(3);
            assertThat(background.getMaxPoolSize()).isEqualTo(3);
            assertThat(background.getQueueCapacity()).isEqualTo(3);
            var poller = context.getBean(EventOutboxPoller.class);
            var relay = context.getBean(EventOutboxRelayService.class);
            var policy = context.getBean(OutboxEventRoutingPolicy.class);

            poller.pollRealtime();
            poller.pollBackground();

            verify(relay, timeout(5000).times(2)).relay(OutboxProcessingGroup.REALTIME,
                policy.selectionFor(OutboxProcessingGroup.REALTIME), 100);
            verify(relay, timeout(5000).times(3)).relay(OutboxProcessingGroup.BACKGROUND,
                policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 100);
        });
    }

    @Test
    @DisplayName("유효하지 않은 동시성 설정이면 시작을 거부한다")
    void 잘못된_동시성_설정_거부() {
        runner.withPropertyValues("app.event-outbox.workers.realtime.concurrency=0").run(context ->
            assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("각 폴링은 독립 executor에서 자기 그룹과 배치 크기로 Relay를 호출한다")
    void 그룹별_워커_연결() {
        runner.withPropertyValues(
            "app.event-outbox.workers.realtime.batch-size=7",
            "app.event-outbox.workers.background.batch-size=13"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            var poller = context.getBean(EventOutboxPoller.class);
            var relay = context.getBean(EventOutboxRelayService.class);
            var policy = context.getBean(OutboxEventRoutingPolicy.class);
            var realtime = context.getBean("realtimeOutboxExecutor", ThreadPoolTaskExecutor.class);
            var background = context.getBean("backgroundOutboxExecutor", ThreadPoolTaskExecutor.class);
            assertThat(realtime).isNotSameAs(background);
            assertThat(realtime.getCorePoolSize()).isEqualTo(3);
            assertThat(background.getCorePoolSize()).isEqualTo(2);
            assertThat(realtime.getThreadNamePrefix()).isEqualTo("outbox-realtime-");
            assertThat(background.getThreadNamePrefix()).isEqualTo("outbox-background-");

            poller.pollRealtime();
            poller.pollBackground();

            verify(relay, timeout(5000).times(3)).relay(
                OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 7
            );
            verify(relay, timeout(5000).times(2)).relay(
                OutboxProcessingGroup.BACKGROUND, policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 13
            );
        });
    }

    @Test
    @DisplayName("기존 공통 주기와 배치 크기를 그룹별 기본값으로 유지한다")
    void 기존_설정_호환() {
        runner.withPropertyValues(
            "app.event-outbox.poll-interval-ms=100",
            "app.event-outbox.batch-size=23",
            "app.event-outbox.workers.realtime.poll-interval-ms=200"
        ).run(context -> {
            var poller = context.getBean(EventOutboxPoller.class);
            var relay = context.getBean(EventOutboxRelayService.class);
            var policy = context.getBean(OutboxEventRoutingPolicy.class);
            String realtimeExpression = EventOutboxPoller.class.getMethod("pollRealtime")
                .getAnnotation(Scheduled.class).fixedDelayString();
            String backgroundExpression = EventOutboxPoller.class.getMethod("pollBackground")
                .getAnnotation(Scheduled.class).fixedDelayString();
            assertThat(context.getEnvironment().resolveRequiredPlaceholders(realtimeExpression)).isEqualTo("200");
            assertThat(context.getEnvironment().resolveRequiredPlaceholders(backgroundExpression)).isEqualTo("100");

            poller.pollRealtime();
            poller.pollBackground();

            verify(relay, timeout(5000).times(3)).relay(
                OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 23
            );
            verify(relay, timeout(5000).times(2)).relay(
                OutboxProcessingGroup.BACKGROUND, policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 23
            );
        });
    }

    @Test
    @DisplayName("기존 relay 비활성 설정이면 폴러를 등록하지 않고 이벤트도 처리하지 않는다")
    void 폴링_비활성() {
        runner.withPropertyValues("app.event-outbox.relay-enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(EventOutboxPoller.class);
            verifyNoInteractions(context.getBean(EventOutboxRelayService.class));
        });
    }
}
