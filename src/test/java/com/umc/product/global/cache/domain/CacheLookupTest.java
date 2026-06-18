package com.umc.product.global.cache.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheLookup")
class CacheLookupTest {

    @Test
    @DisplayName("Hit은 값을 변환할 수 있다")
    void hit_map() {
        CacheLookup<Integer> lookup = new CacheLookup.Hit<>("abc").map(String::length);

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup).isInstanceOf(CacheLookup.Hit.class);
        assertThat(((CacheLookup.Hit<Integer>) lookup).value()).isEqualTo(3);
    }

    @Test
    @DisplayName("Miss는 변환해도 Miss로 유지된다")
    void miss_map() {
        CacheLookup<Integer> lookup = new CacheLookup.Miss<String>().map(String::length);

        assertThat(lookup.hit()).isFalse();
        assertThat(lookup).isInstanceOf(CacheLookup.Miss.class);
    }
}
