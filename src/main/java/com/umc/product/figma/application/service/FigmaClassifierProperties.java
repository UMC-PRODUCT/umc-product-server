package com.umc.product.figma.application.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Figma 댓글 분류기 운영 파라미터 (LLM_분류_캐시_점검_보고서 §3.7 권고 반영).
 *
 * @param cache L1 Caffeine 캐시 설정.
 */
@ConfigurationProperties(prefix = "app.figma.classifier")
public record FigmaClassifierProperties(
    Cache cache
) {

    public FigmaClassifierProperties {
        if (cache == null) {
            cache = Cache.defaults();
        }
    }

    /**
     * @param maxSize       최대 항목 수. 초과 시 W-TinyLFU 정책으로 eviction.
     * @param ttl           {@code expireAfterWrite} TTL. positive / negative 응답 모두 동일 TTL 적용.
     */
    public record Cache(
        long maxSize,
        Duration ttl
    ) {

        private static final long DEFAULT_MAX_SIZE = 10_000L;
        private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

        public Cache {
            if (maxSize <= 0) {
                maxSize = DEFAULT_MAX_SIZE;
            }
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                ttl = DEFAULT_TTL;
            }
        }

        public static Cache defaults() {
            return new Cache(DEFAULT_MAX_SIZE, DEFAULT_TTL);
        }
    }
}
