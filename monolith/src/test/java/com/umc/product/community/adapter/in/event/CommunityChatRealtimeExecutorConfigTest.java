package com.umc.product.community.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.p6spy.engine.spy.P6DataSource;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class CommunityChatRealtimeExecutorConfigTest {

    @Mock
    private DataSource delegate;

    @Mock
    private Connection connection;

    @Test
    @DisplayName("전송 작업의 큐 대기와 JDBC 연결 획득을 별도로 기록한다")
    void measuresQueueWaitAndJdbcAcquisitionSeparately() throws Exception {
        // given
        given(delegate.getConnection()).willReturn(connection);
        CommunityChatRealtimeExecutorConfig config = new CommunityChatRealtimeExecutorConfig();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        P6DataSource dataSource = new P6DataSource(delegate);
        var listener = config.communityChatRealtimeConnectionMetrics(registry);
        dataSource.setJdbcEventListenerFactory(() -> listener);
        ThreadPoolTaskExecutor executor = config.communityChatRealtimeExecutor(registry);
        executor.initialize();
        try {
            // when
            dataSource.getConnection().close();
            assertThat(registry.get("community.chat.realtime.jdbc.acquire").timer().count()).isZero();
            executor.submit(() -> {
                dataSource.getConnection().close();
                return null;
            }).get(5, TimeUnit.SECONDS);

            // then
            assertThat(registry.get("community.chat.realtime.queue.wait").timer().count()).isEqualTo(1);
            assertThat(registry.get("community.chat.realtime.jdbc.acquire").timer().count()).isEqualTo(1);
        } finally {
            executor.shutdown();
            registry.close();
        }
    }
}
