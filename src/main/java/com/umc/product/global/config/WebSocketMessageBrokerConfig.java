package com.umc.product.global.config;

import com.umc.product.global.security.StompChannelInterceptor;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;
    private final ObservationRegistry observationRegistry;
    private final ContextSnapshotFactory snapshotFactory;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .withSockJS();
    }

    @Bean
    public ThreadPoolTaskScheduler webSocketHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic/", "/queue/")
            .setHeartbeatValue(new long[]{4000, 4000})
            .setTaskScheduler(webSocketHeartbeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
        registration.taskExecutor()
            .corePoolSize(32)
            .maxPoolSize(32)
            .queueCapacity(2048)
            .keepAliveSeconds(60);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(4096);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ws-outbound-");
        executor.setTaskDecorator(runnable -> {
            ContextSnapshot snapshot = snapshotFactory.captureAll();
            return snapshot.wrap(() -> {
                Observation parent = observationRegistry.getCurrentObservation();
                if (parent != null) {
                    Observation.createNotStarted("websocket.outbound", observationRegistry)
                        .parentObservation(parent)
                        .lowCardinalityKeyValue("thread", Thread.currentThread().getName())
                        .observe(runnable);
                } else {
                    runnable.run();
                }
            });
        });
        executor.initialize();
        registration.taskExecutor(executor);
    }
}
