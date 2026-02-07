package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuNameInfo;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class GetGisuUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetGisuUseCase getGisuUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Test
    void 전체_기수_목록을_조회한다() {
        // given
        manageGisuPort.save(createGisu(7L, false));
        manageGisuPort.save(createGisu(8L, true));
        manageGisuPort.save(createGisu(9L, false));

        // when
        List<GisuInfo> result = getGisuUseCase.getList();

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void 활성_기수와_비활성_기수를_구분할_수_있다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createGisu(8L, true));
        Gisu inactiveGisu = manageGisuPort.save(createGisu(7L, false));

        // when
        GisuInfo activeInfo = getGisuUseCase.getById(activeGisu.getId());
        GisuInfo inactiveInfo = getGisuUseCase.getById(inactiveGisu.getId());

        // then
        assertThat(activeInfo.isActive()).isTrue();
        assertThat(inactiveInfo.isActive()).isFalse();
    }

    @Test
    void 기수_정보에_시작일과_종료일이_포함된다() {
        // given
        Instant startAt = Instant.parse("2024-03-01T00:00:00Z");
        Instant endAt = Instant.parse("2024-08-31T23:59:59Z");
        Gisu gisu = manageGisuPort.save(Gisu.create(8L, startAt, endAt, true));

        // when
        GisuInfo result = getGisuUseCase.getById(gisu.getId());

        // then
        assertThat(result.startAt()).isEqualTo(startAt);
        assertThat(result.endAt()).isEqualTo(endAt);
    }

    @Test
    void 기수_목록을_페이징하여_조회한다() {
        // given
        for (int i = 1; i <= 5; i++) {
            manageGisuPort.save(createGisu((long) i, false));
        }

        PageRequest pageRequest = PageRequest.of(0, 3);

        // when
        Page<GisuInfo> result = getGisuUseCase.getList(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    void 기수_페이징_마지막_페이지를_조회한다() {
        // given
        for (int i = 1; i <= 5; i++) {
            manageGisuPort.save(createGisu((long) i, false));
        }

        PageRequest pageRequest = PageRequest.of(1, 3);

        // when
        Page<GisuInfo> result = getGisuUseCase.getList(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    void 기수가_없으면_빈_페이지를_반환한다() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<GisuInfo> result = getGisuUseCase.getList(pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 페이징_결과가_generation_내림차순으로_정렬된다() {
        // given
        manageGisuPort.save(createGisu(7L, false));
        manageGisuPort.save(createGisu(9L, true));
        manageGisuPort.save(createGisu(8L, false));

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<GisuInfo> result = getGisuUseCase.getList(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).generation()).isEqualTo(9L);
        assertThat(result.getContent().get(1).generation()).isEqualTo(8L);
        assertThat(result.getContent().get(2).generation()).isEqualTo(7L);
    }

    @Test
    void 전체_기수_이름_목록을_조회한다() {
        // given
        manageGisuPort.save(createGisu(7L, false));
        manageGisuPort.save(createGisu(9L, true));
        manageGisuPort.save(createGisu(8L, false));

        // when
        List<GisuNameInfo> result = getGisuUseCase.getAllGisuNames();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(GisuNameInfo::generation)
                .containsExactly(9L, 8L, 7L);
    }

    @Test
    void 기수가_없으면_빈_이름_목록을_반환한다() {
        // when
        List<GisuNameInfo> result = getGisuUseCase.getAllGisuNames();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 활성화된_기수를_조회한다() {
        // given
        manageGisuPort.save(createGisu(7L, false));
        Gisu activeGisu = manageGisuPort.save(createGisu(8L, true));

        // when
        GisuInfo result = getGisuUseCase.getActiveGisu();

        // then
        assertThat(result.gisuId()).isEqualTo(activeGisu.getId());
        assertThat(result.generation()).isEqualTo(8L);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void 활성화된_기수가_없으면_예외가_발생한다() {
        // given
        manageGisuPort.save(createGisu(7L, false));

        // when & then
        assertThatThrownBy(() -> getGisuUseCase.getActiveGisu())
                .isInstanceOf(com.umc.product.global.exception.BusinessException.class);
    }

    private Gisu createGisu(Long generation, boolean isActive) {
        return Gisu.create(
                generation,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                isActive
        );
    }
}
