package com.umc.product.community.adapter.in.event;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Configuration(proxyBeanMethods = false)
class CommunityChatRealtimeExecutorConfig {

    private static final String THREAD_NAME_PREFIX = "community-chat-realtime-";

    @Bean
    ThreadPoolTaskExecutor communityChatRealtimeExecutor(MeterRegistry registry) {
        Timer queueWait = Timer.builder("community.chat.realtime.queue.wait")
            .description("채팅 실시간 전송 작업 등록부터 실행 시작까지 걸린 시간")
            .publishPercentileHistogram()
            .register(registry);
        ContextPropagatingTaskDecorator contextDecorator = new ContextPropagatingTaskDecorator();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setTaskDecorator(task -> {
            long submittedAt = System.nanoTime();
            return contextDecorator.decorate(() -> {
                queueWait.record(System.nanoTime() - submittedAt, TimeUnit.NANOSECONDS);
                task.run();
            });
        });
        return executor;
    }

    @Bean
    JdbcEventListener communityChatRealtimeConnectionMetrics(MeterRegistry registry) {
        Timer acquireTime = Timer.builder("community.chat.realtime.jdbc.acquire")
            .description("채팅 실시간 전송 실행기에서 JDBC 연결 획득을 시도한 시간")
            .publishPercentileHistogram()
            .register(registry);
        return new JdbcEventListener() {
            @Override
            public void onAfterGetConnection(ConnectionInformation info, SQLException exception) {
                if (Thread.currentThread().getName().startsWith(THREAD_NAME_PREFIX)) {
                    acquireTime.record(info.getTimeToGetConnectionNs(), TimeUnit.NANOSECONDS);
                }
            }
        };
    }
}
