package com.umc.product.figma.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.figma.application.service.FigmaClassifierProperties;
import com.umc.product.global.cache.application.port.in.CacheUseCase;
import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheLookup;
import com.umc.product.global.cache.domain.CacheNamespace;
import com.umc.product.global.cache.domain.CacheSpec;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FigmaClassificationCacheAdapter")
class FigmaClassificationCacheAdapterTest {

    @Test
    @DisplayName("positive classification cache를 저장하고 조회한다")
    void positive_cache() {
        FakeCacheUseCase cacheUseCase = new FakeCacheUseCase();
        FigmaClassificationCacheAdapter adapter = newAdapter(cacheUseCase);

        adapter.put("comment-1", Optional.of("auth"));

        assertThat(adapter.contains("comment-1")).isTrue();
        assertThat(adapter.get("comment-1")).contains("auth");
        assertThat(cacheUseCase.lastSpec.namespace()).isEqualTo(CacheNamespace.FIGMA_CLASSIFICATION);
        assertThat(cacheUseCase.lastSpec.maximumSize()).isEqualTo(50L);
    }

    @Test
    @DisplayName("negative classification cache는 contains=true와 Optional.empty로 조회된다")
    void negative_cache() {
        FigmaClassificationCacheAdapter adapter = newAdapter(new FakeCacheUseCase());

        adapter.put("comment-1", Optional.empty());

        assertThat(adapter.contains("comment-1")).isTrue();
        assertThat(adapter.get("comment-1")).isEmpty();
    }

    @Test
    @DisplayName("저장되지 않은 commentId는 contains=false이다")
    void cache_miss() {
        FigmaClassificationCacheAdapter adapter = newAdapter(new FakeCacheUseCase());

        assertThat(adapter.contains("missing")).isFalse();
        assertThat(adapter.get("missing")).isEmpty();
    }

    private FigmaClassificationCacheAdapter newAdapter(FakeCacheUseCase cacheUseCase) {
        FigmaClassifierProperties properties = new FigmaClassifierProperties(
            new FigmaClassifierProperties.Cache(50L, Duration.ofMinutes(3))
        );
        return new FigmaClassificationCacheAdapter(cacheUseCase, properties);
    }

    private static class FakeCacheUseCase implements CacheUseCase {

        private final Map<String, Object> storage = new HashMap<>();
        private CacheSpec<?> lastSpec;

        @Override
        public <T> CacheLookup<T> get(CacheSpec<T> spec, CacheKey key) {
            this.lastSpec = spec;
            Object value = storage.get(key.value());
            if (value == null) {
                return new CacheLookup.Miss<>();
            }
            return new CacheLookup.Hit<>(spec.valueType().cast(value));
        }

        @Override
        public <T> void put(CacheSpec<T> spec, CacheKey key, T value) {
            this.lastSpec = spec;
            storage.put(key.value(), value);
        }

        @Override
        public void evict(CacheNamespace namespace, CacheKey key) {
            storage.remove(key.value());
        }
    }
}
