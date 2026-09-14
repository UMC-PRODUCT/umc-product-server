package com.umc.product.global.cache.adapter.out;

import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheNamespace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheKeyFormatter {

    private final String environment;

    public CacheKeyFormatter(@Value("${app.environment:local}") String environment) {
        this.environment = environment;
    }

    public String format(CacheNamespace namespace, CacheKey key) {
        return "umc:%s:%s:%s".formatted(environment, namespace.value(), key.value());
    }
}
