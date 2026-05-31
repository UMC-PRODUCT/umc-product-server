package com.umc.product.figma.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ClassificationCacheValue")
class ClassificationCacheValueTest {

    @Test
    @DisplayName("positive cache 값은 domainKey를 Optional로 복원한다")
    void positive_cache_value() {
        ClassificationCacheValue value = ClassificationCacheValue.from(Optional.of("auth"));

        assertThat(value.classified()).isTrue();
        assertThat(value.toOptional()).contains("auth");
    }

    @Test
    @DisplayName("negative cache 값은 hit 상태를 유지하되 Optional.empty로 복원한다")
    void negative_cache_value() {
        ClassificationCacheValue value = ClassificationCacheValue.from(Optional.empty());

        assertThat(value.classified()).isFalse();
        assertThat(value.toOptional()).isEmpty();
    }
}
