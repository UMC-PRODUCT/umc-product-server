package com.umc.product.global.cache.domain;

import java.time.Duration;

public record CacheSpec<T>(
    CacheNamespace namespace,
    Class<T> valueType,
    Duration ttl,
    long maximumSize
) {

    public CacheSpec {
        if (namespace == null) {
            throw new IllegalArgumentException("cache namespace는 필수입니다.");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("cache valueType은 필수입니다.");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("cache ttl은 양수여야 합니다.");
        }
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("cache maximumSize는 양수여야 합니다.");
        }
    }

    public static <T> CacheSpec<T> of(
        CacheNamespace namespace,
        Class<T> valueType,
        Duration ttl,
        long maximumSize
    ) {
        return new CacheSpec<>(namespace, valueType, ttl, maximumSize);
    }
}
