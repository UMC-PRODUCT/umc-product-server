package com.umc.product.global.cache.application.service;

import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.application.port.out.CacheStorePort;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService implements CacheUseCase {

    private final CacheStorePort cacheStorePort;

    @Override
    public <T> CacheLookup<T> get(CacheSpec<T> spec, CacheKey key) {
        return cacheStorePort.get(spec, key);
    }

    @Override
    public <T> void put(CacheSpec<T> spec, CacheKey key, T value) {
        cacheStorePort.put(spec, key, value);
    }

    @Override
    public void evict(CacheNamespace namespace, CacheKey key) {
        cacheStorePort.evict(namespace, key);
    }
}
