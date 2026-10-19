package com.umc.product.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.global.event.application.service.OutboxWorker;

@Configuration(proxyBeanMethods = false)
public class OutboxWorkerConfig {

    @Bean
    public ThreadPoolTaskExecutor realtimeOutboxExecutor() {
        return createExecutor("outbox-realtime-");
    }

    @Bean
    public ThreadPoolTaskExecutor backgroundOutboxExecutor() {
        return createExecutor("outbox-background-");
    }

    @Bean(destroyMethod = "close")
    public OutboxWorker realtimeOutboxWorker(
        @Qualifier("realtimeOutboxExecutor") ThreadPoolTaskExecutor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        @Value("${app.event-outbox.workers.realtime.batch-size:${app.event-outbox.batch-size:100}}") int batchSize
    ) {
        return new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, routingPolicy, batchSize);
    }

    @Bean(destroyMethod = "close")
    public OutboxWorker backgroundOutboxWorker(
        @Qualifier("backgroundOutboxExecutor") ThreadPoolTaskExecutor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        @Value("${app.event-outbox.workers.background.batch-size:${app.event-outbox.batch-size:100}}") int batchSize
    ) {
        return new OutboxWorker(OutboxProcessingGroup.BACKGROUND, executor, relay, routingPolicy, batchSize);
    }

    private ThreadPoolTaskExecutor createExecutor(String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
