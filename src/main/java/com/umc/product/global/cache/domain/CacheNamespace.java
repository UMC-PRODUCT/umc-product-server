package com.umc.product.global.cache.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CacheNamespace {
    FIGMA_CLASSIFICATION("figma.classification");

    private final String value;

    CacheNamespace(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static void validateUniqueValues() {
        Map<String, Long> counts = Arrays.stream(values())
            .map(CacheNamespace::value)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        counts.forEach((namespace, count) -> {
            if (count > 1) {
                throw new IllegalStateException("중복 cache namespace 입니다. namespace=" + namespace);
            }
        });
    }
}
