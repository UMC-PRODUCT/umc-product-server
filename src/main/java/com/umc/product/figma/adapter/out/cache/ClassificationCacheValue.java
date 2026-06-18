package com.umc.product.figma.adapter.out.cache;

import java.util.Optional;

public record ClassificationCacheValue(
    boolean classified,
    String domainKey
) {

    public ClassificationCacheValue {
        if (classified && (domainKey == null || domainKey.isBlank())) {
            throw new IllegalArgumentException("classified cache value는 domainKey가 필요합니다.");
        }
    }

    public static ClassificationCacheValue from(Optional<String> domainKey) {
        return new ClassificationCacheValue(domainKey.isPresent(), domainKey.orElse(null));
    }

    public Optional<String> toOptional() {
        if (!classified) {
            return Optional.empty();
        }
        return Optional.of(domainKey);
    }
}
