package com.umc.product.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaintenanceDomainSetConverterTest {

    private final MaintenanceDomainSetConverter sut = new MaintenanceDomainSetConverter();

    @Test
    @DisplayName("Set 을 정렬된 콤마 문자열로 변환")
    void to_db() {
        String result = sut.convertToDatabaseColumn(
            EnumSet.of(MaintenanceDomain.PROJECT, MaintenanceDomain.CHALLENGER)
        );
        assertThat(result).isEqualTo("CHALLENGER,PROJECT");
    }

    @Test
    @DisplayName("빈 Set 은 null")
    void empty_to_null() {
        assertThat(sut.convertToDatabaseColumn(EnumSet.noneOf(MaintenanceDomain.class)))
            .isNull();
        assertThat(sut.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("DB 문자열을 EnumSet 으로 복원")
    void from_db() {
        assertThat(sut.convertToEntityAttribute("CHALLENGER,PROJECT"))
            .containsExactlyInAnyOrder(MaintenanceDomain.CHALLENGER, MaintenanceDomain.PROJECT);
    }

    @Test
    @DisplayName("null/공백 은 빈 EnumSet")
    void null_or_blank() {
        assertThat(sut.convertToEntityAttribute(null)).isEmpty();
        assertThat(sut.convertToEntityAttribute("")).isEmpty();
        assertThat(sut.convertToEntityAttribute("   ")).isEmpty();
    }
}
