package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

class QaGisuIdLoaderTest {

    private final GetGisuUseCase getGisuUseCase = mock(GetGisuUseCase.class);
    private final QaGisuIdLoader loader = new QaGisuIdLoader(getGisuUseCase);

    @Test
    @DisplayName("요청한 기수만 기수 번호별 ID로 돌려주고, 없는 기수는 빠진다")
    void returnsRequestedGenerationsOnly() {
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 9L), gisu(2L, 10L), gisu(3L, 11L)));

        assertThat(loader.load(List.of(10, 11, 12))).isEqualTo(Map.of(10, 2L, 11, 3L));
    }

    @Test
    @DisplayName("요청한 기수가 둘이면 어느 쪽도 고르지 않고 실패한다")
    void rejectsDuplicatedRequestedGeneration() {
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 11L), gisu(2L, 11L)));

        assertThatThrownBy(() -> loader.load(List.of(11)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("11기");
    }

    @Test
    @DisplayName("요청하지 않은 기수의 중복은 무시한다")
    void ignoresDuplicatesOutsideRequest() {
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 10L), gisu(2L, 10L), gisu(3L, 11L)));

        assertThat(loader.load(List.of(11))).isEqualTo(Map.of(11, 3L));
    }

    private static GisuInfo gisu(Long id, Long generation) {
        return new GisuInfo(id, generation, null, null, false);
    }
}
