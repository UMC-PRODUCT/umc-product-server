package com.umc.product.global.cache.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheKey")
class CacheKeyTest {

    @Test
    @DisplayName("문자열 값으로 cache key를 생성한다")
    void cache_key_생성() {
        CacheKey key = CacheKey.from("comment-1");

        assertThat(key.value()).isEqualTo("comment-1");
    }

    @Test
    @DisplayName("빈 cache key는 허용하지 않는다")
    void 빈_cache_key_거부() {
        assertThatThrownBy(() -> CacheKey.from(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("cache key는 비어 있을 수 없습니다.");
    }
}
