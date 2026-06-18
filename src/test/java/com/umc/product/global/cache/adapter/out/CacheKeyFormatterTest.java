package com.umc.product.global.cache.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.global.cache.domain.CacheKey;
import com.umc.product.global.cache.domain.CacheNamespace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheKeyFormatter")
class CacheKeyFormatterTest {

    @Test
    @DisplayName("환경, namespace, key를 조합해 최종 cache key를 만든다")
    void cache_key_format() {
        CacheKeyFormatter formatter = new CacheKeyFormatter("local");

        String result = formatter.format(CacheNamespace.FIGMA_CLASSIFICATION, CacheKey.from("comment-1"));

        assertThat(result).isEqualTo("umc:local:figma.classification:comment-1");
    }
}
