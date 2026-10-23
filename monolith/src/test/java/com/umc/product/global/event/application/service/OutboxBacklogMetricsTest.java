package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort;
import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort.OutboxBacklog;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox 그룹별 적체 관측")
class OutboxBacklogMetricsTest {

    @Mock
    private LoadOutboxBacklogPort port;

    @Test
    @DisplayName("그룹별 대기와 처리 중 건수 및 최고 대기 시간을 구분한다")
    void 그룹별_적체_분리() {
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();
        var registry = new SimpleMeterRegistry();
        var metrics = new OutboxBacklogMetrics(port, policy, registry);
        when(port.getBacklog(policy.selectionFor(OutboxProcessingGroup.REALTIME)))
            .thenReturn(new OutboxBacklog(3, 1, 12));
        when(port.getBacklog(policy.selectionFor(OutboxProcessingGroup.BACKGROUND)))
            .thenReturn(new OutboxBacklog(20, 2, 120));

        metrics.refresh();

        assertThat(registry.get("event.outbox.backlog").tags("group", "realtime", "status", "pending")
            .gauge().value()).isEqualTo(3);
        assertThat(registry.get("event.outbox.backlog").tags("group", "background", "status", "processing")
            .gauge().value()).isEqualTo(2);
        assertThat(registry.get("event.outbox.oldest.age").tag("group", "background")
            .gauge().value()).isEqualTo(120);
        assertThat(registry.get("event.outbox.backlog.sample.timestamp").tag("group", "realtime")
            .gauge().value()).isPositive();
    }

    @Test
    @DisplayName("수집 실패 시 이전 값과 수집 시각을 유지하고 실패 횟수를 기록한다")
    void 수집실패_구분() {
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();
        var registry = new SimpleMeterRegistry();
        var metrics = new OutboxBacklogMetrics(port, policy, registry);
        when(port.getBacklog(policy.selectionFor(OutboxProcessingGroup.REALTIME)))
            .thenReturn(new OutboxBacklog(3, 0, 12)).thenThrow(new IllegalStateException("조회 실패"));
        when(port.getBacklog(policy.selectionFor(OutboxProcessingGroup.BACKGROUND)))
            .thenReturn(new OutboxBacklog(0, 0, 0));
        metrics.refresh();
        double sampledAt = registry.get("event.outbox.backlog.sample.timestamp")
            .tag("group", "realtime").gauge().value();

        metrics.refresh();

        assertThat(registry.get("event.outbox.backlog.sample.timestamp").tag("group", "realtime")
            .gauge().value()).isEqualTo(sampledAt);
        assertThat(registry.get("event.outbox.backlog.sample.failure").tag("group", "realtime")
            .counter().count()).isEqualTo(1);
    }
}
