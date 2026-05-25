package com.umc.product.figma.adapter.out.cache;

import com.umc.product.figma.application.port.out.FigmaClassificationCachePort;
import com.umc.product.figma.application.service.FigmaClassifierProperties;
import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FigmaClassificationCacheAdapter implements FigmaClassificationCachePort {

    private final CacheUseCase cacheUseCase;
    private final CacheSpec<ClassificationCacheValue> spec;

    public FigmaClassificationCacheAdapter(CacheUseCase cacheUseCase, FigmaClassifierProperties properties) {
        this.cacheUseCase = cacheUseCase;
        this.spec = CacheSpec.of(
            CacheNamespace.FIGMA_CLASSIFICATION,
            ClassificationCacheValue.class,
            properties.cache().ttl(),
            properties.cache().maxSize()
        );
    }

    @Override
    public Optional<String> get(String commentId) {
        CacheLookup<ClassificationCacheValue> lookup = cacheUseCase.get(spec, CacheKey.from(commentId));
        if (lookup instanceof CacheLookup.Hit<ClassificationCacheValue> hit) {
            return hit.value().toOptional();
        }
        return Optional.empty();
    }

    @Override
    public boolean contains(String commentId) {
        return cacheUseCase.get(spec, CacheKey.from(commentId)).hit();
    }

    @Override
    public void put(String commentId, Optional<String> domainKey) {
        cacheUseCase.put(spec, CacheKey.from(commentId), ClassificationCacheValue.from(domainKey));
    }
}
