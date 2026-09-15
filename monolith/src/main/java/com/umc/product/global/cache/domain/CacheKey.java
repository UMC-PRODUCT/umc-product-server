package com.umc.product.global.cache.domain;

public record CacheKey(String value) {

    public CacheKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("cache key는 비어 있을 수 없습니다.");
        }
    }

    public static CacheKey from(String value) {
        return new CacheKey(value);
    }
}
