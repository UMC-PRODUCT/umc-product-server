package com.umc.product.organization.application.port.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("GisuQueryService")
class GisuQueryServiceTest {

    @Mock
    LoadGisuPort loadGisuPort;

    @InjectMocks
    GisuQueryService gisuQueryService;

    @Test
    @DisplayName("batchGetByIds는 중복을 제거하고 요청 순서를 보존한다")
    void batchGetByIds는_중복을_제거하고_요청_순서를_보존한다() {
        given(loadGisuPort.listByIds(new LinkedHashSet<>(List.of(2L, 1L)))).willReturn(List.of(
            gisu(1L, 9L, false),
            gisu(2L, 10L, true)
        ));

        List<GisuInfo> result = gisuQueryService.batchGetByIds(List.of(2L, 1L, 2L));

        assertThat(result).extracting(GisuInfo::gisuId).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("batchGetByGenerations는 중복을 제거하고 요청 순서를 보존한다")
    void batchGetByGenerations는_중복을_제거하고_요청_순서를_보존한다() {
        given(loadGisuPort.listByGenerations(new LinkedHashSet<>(List.of(10L, 9L)))).willReturn(List.of(
            gisu(1L, 9L, false),
            gisu(2L, 10L, true)
        ));

        List<GisuInfo> result = gisuQueryService.batchGetByGenerations(List.of(10L, 9L, 10L));

        assertThat(result).extracting(GisuInfo::generation).containsExactly(10L, 9L);
    }

    @Test
    @DisplayName("batchGetByIds는 요청한 기수가 하나라도 없으면 GISU_NOT_FOUND를 던진다")
    void batchGetByIds는_요청한_기수가_하나라도_없으면_GISU_NOT_FOUND를_던진다() {
        given(loadGisuPort.listByIds(new LinkedHashSet<>(List.of(1L, 999L)))).willReturn(List.of(
            gisu(1L, 9L, false)
        ));

        assertThatThrownBy(() -> gisuQueryService.batchGetByIds(List.of(1L, 999L)))
            .isInstanceOf(OrganizationDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", OrganizationErrorCode.GISU_NOT_FOUND);
    }

    private Gisu gisu(Long id, Long generation, boolean active) {
        Gisu gisu = Gisu.create(
            generation,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T23:59:59Z"),
            active
        );
        ReflectionTestUtils.setField(gisu, "id", id);
        return gisu;
    }
}
