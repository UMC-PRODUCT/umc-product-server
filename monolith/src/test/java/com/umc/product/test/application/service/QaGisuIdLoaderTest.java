package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
class QaGisuIdLoaderTest {

    @Mock private GetGisuUseCase getGisuUseCase;

    @InjectMocks private QaGisuIdLoader loader;

    @Test
    @DisplayName("요청한 기수만 기수 번호별 ID로 돌려주고, 없는 기수는 빠진다")
    void returnsRequestedGenerationsOnly() {
        // given
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 9L), gisu(2L, 10L), gisu(3L, 11L)));

        // when
        Map<Integer, Long> gisuIds = loader.load(List.of(10, 11, 12));

        // then
        assertThat(gisuIds).isEqualTo(Map.of(10, 2L, 11, 3L));
    }

    @Test
    @DisplayName("요청한 기수의 행이 둘이면 어느 쪽도 고르지 않고 실패한다")
    void rejectsDuplicatedRequestedGeneration() {
        // given
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 11L), gisu(2L, 11L)));

        // when & then
        assertThatThrownBy(() -> loader.load(List.of(11)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("11기");
    }

    @Test
    @DisplayName("요청하지 않은 기수의 중복은 무시한다")
    void ignoresDuplicatesOutsideRequest() {
        // given
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 10L), gisu(2L, 10L), gisu(3L, 11L)));

        // when
        Map<Integer, Long> gisuIds = loader.load(List.of(11));

        // then
        assertThat(gisuIds).isEqualTo(Map.of(11, 3L));
    }

    @Test
    @DisplayName("한 기수를 찾을 때 그 기수가 없으면 빈 값을 돌려준다")
    void findReturnsEmptyWhenGenerationAbsent() {
        // given
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 10L)));

        // when & then
        assertThat(loader.find(11)).isEmpty();
    }

    @Test
    @DisplayName("한 기수를 찾을 때 그 기수의 행이 둘이면 실패한다")
    void findRejectsDuplicatedGeneration() {
        // given
        given(getGisuUseCase.getList()).willReturn(List.of(gisu(1L, 11L), gisu(2L, 11L)));

        // when & then
        assertThatThrownBy(() -> loader.find(11))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("11기");
    }

    private static GisuInfo gisu(Long id, Long generation) {
        return new GisuInfo(id, generation, null, null, false);
    }
}
