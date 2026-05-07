package com.umc.product.figma.application.service;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Figma 댓글 분류기의 L1 Caffeine 캐시를 Micrometer 에 노출하는 설정.
 * <p>
 * {@code figma.classifier.l1.*} prefix 로 다음 메트릭이 자동 노출된다 (CaffeineCacheMetrics 표준):
 * <ul>
 *   <li>{@code cache.size} — 현재 캐시 항목 수</li>
 *   <li>{@code cache.gets{result=hit|miss}} — hit/miss 카운터</li>
 *   <li>{@code cache.puts} — put 카운터</li>
 *   <li>{@code cache.evictions{cause=...}} — eviction 카운터 (size, expired 등)</li>
 *   <li>{@code cache.eviction.weight} — 누적 eviction weight</li>
 * </ul>
 * 운영자는 위 메트릭으로 hit rate / miss rate / eviction 패턴을 직접 측정할 수 있다 (LLM_분류_캐시_점검_보고서 §3.3).
 */
@Configuration
public class FigmaClassifierMetricsConfig {

    @Bean
    MeterBinder figmaClassifierCacheMetrics(FigmaCommentDomainClassifier classifier) {
        return registry -> CaffeineCacheMetrics.monitor(registry, classifier.getCache(), "figma.classifier.l1");
    }
}
