package com.umc.product.global.cache.application.port.in;

import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;

public interface CacheUseCase {

    <T> CacheLookup<T> get(CacheSpec<T> spec, CacheKey key);

    <T> void put(CacheSpec<T> spec, CacheKey key, T value);

    void evict(CacheNamespace namespace, CacheKey key);
}
