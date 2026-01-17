package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        LocalDateTime startAt = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 8, 31, 23, 59);
        Gisu gisu = manageGisuPort.save(Gisu.builder()
                .generation(8L)
                .isActive(true)
                .startAt(startAt)
                .endAt(endAt)
                .build());

        // when
        GisuInfo result = getGisuUseCase.getById(gisu.getId());

        // then
        assertThat(result.startAt()).isEqualTo(startAt.toLocalDate());
        assertThat(result.endAt()).isEqualTo(endAt.toLocalDate());
    }

    private Gisu createGisu(Long generation, boolean isActive) {
        return Gisu.builder()
                .generation(generation)
                .isActive(isActive)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }
}
