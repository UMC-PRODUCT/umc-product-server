package com.umc.product.global.config;

import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy.Rule;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;

@Configuration(proxyBeanMethods = false)
public class OutboxRoutingConfig {

    @Bean
    public OutboxEventRoutingPolicy outboxEventRoutingPolicy() {
        OutboxProcessingGroup realtime = OutboxProcessingGroup.REALTIME;
        OutboxProcessingGroup background = OutboxProcessingGroup.BACKGROUND;

        return new OutboxEventRoutingPolicy(Set.of(realtime, background), background, List.of(
            new Rule("community.thread.updated", realtime),
            new Rule("community.thread.deleted", realtime),
            new Rule("community.thread.invited", realtime),
            new Rule("community.thread.member.left", realtime),
            new Rule("community.thread.member.kicked", realtime)
        ), "chat.", realtime);
    }
}
