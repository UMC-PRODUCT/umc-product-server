package com.umc.product.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.umc.product.global.event.application.service.EventOutboxRelayMetrics;
import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.global.event.application.service.OutboxWorker;

@Configuration(proxyBeanMethods = false)
public class OutboxWorkerConfig {

    @Bean
    public ThreadPoolTaskExecutor realtimeOutboxExecutor(
        @Value("${app.event-outbox.workers.realtime.concurrency:3}") int concurrency
    ) {
        return createExecutor("outbox-realtime-", concurrency);
    }

    @Bean
    public ThreadPoolTaskExecutor backgroundOutboxExecutor(
        @Value("${app.event-outbox.workers.background.concurrency:2}") int concurrency
    ) {
        return createExecutor("outbox-background-", concurrency);
    }

    @Bean(destroyMethod = "close")
    public OutboxWorker realtimeOutboxWorker(
        @Qualifier("realtimeOutboxExecutor") ThreadPoolTaskExecutor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        EventOutboxRelayMetrics metrics,
        @Value("${app.event-outbox.workers.realtime.batch-size:${app.event-outbox.batch-size:100}}") int batchSize
    ) {
        return new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, routingPolicy, batchSize, metrics,
            executor.getMaxPoolSize());
    }

    @Bean(destroyMethod = "close")
    public OutboxWorker backgroundOutboxWorker(
        @Qualifier("backgroundOutboxExecutor") ThreadPoolTaskExecutor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        EventOutboxRelayMetrics metrics,
        @Value("${app.event-outbox.workers.background.batch-size:${app.event-outbox.batch-size:100}}") int batchSize
    ) {
        return new OutboxWorker(OutboxProcessingGroup.BACKGROUND, executor, relay, routingPolicy, batchSize, metrics,
            executor.getMaxPoolSize());
    }

    private ThreadPoolTaskExecutor createExecutor(String threadNamePrefix, int concurrency) {
        if (concurrency <= 0) {
            throw new IllegalArgumentException("동시 실행 수는 양수여야 합니다.");
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(concurrency);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
