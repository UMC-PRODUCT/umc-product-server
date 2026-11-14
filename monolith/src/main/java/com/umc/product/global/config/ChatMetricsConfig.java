package com.umc.product.global.config;

import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ConfigurableTransactionManager;

import com.umc.product.global.observability.chat.ChatTransactionMetrics;

@Configuration
public class ChatMetricsConfig {

    @Bean
    SmartInitializingSingleton registerChatTransactionMetrics(
        List<ConfigurableTransactionManager> managers, ChatTransactionMetrics metrics
    ) {
        return () -> managers.forEach(manager -> manager.addListener(metrics));
    }
}
