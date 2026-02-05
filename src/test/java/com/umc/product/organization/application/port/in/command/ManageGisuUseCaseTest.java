package com.umc.product.organization.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageGisuUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageGisuUseCase manageGisuUseCase;

    @Autowired
    private GetGisuUseCase getGisuUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Test
    void 신규_기수를_생성한다() {
        // given
        CreateGisuCommand command = new CreateGisuCommand(
                10L,
                Instant.parse("2025-03-01T00:00:00Z"),
                Instant.parse("2025-08-31T23:59:59Z")
        );

        // when
        Long gisuId = manageGisuUseCase.register(command);

        // then
        assertThat(gisuId).isNotNull();
        GisuInfo savedGisu = getGisuUseCase.getById(gisuId);
        assertThat(savedGisu.generation()).isEqualTo(10L);
        assertThat(savedGisu.isActive()).isFalse();
    }

    @Test
    void 이미_존재하는_기수_번호로_생성하면_예외가_발생한다() {
        // given
        Gisu existingGisu = Gisu.create(
                10L,
                Instant.parse("2025-03-01T00:00:00Z"),
                Instant.parse("2025-08-31T23:59:59Z"),
                false
        );
        manageGisuPort.save(existingGisu);

        CreateGisuCommand command = new CreateGisuCommand(
                10L,
                Instant.parse("2026-03-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z")
        );

        // when & then
        assertThatThrownBy(() -> manageGisuUseCase.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(OrganizationErrorCode.GISU_ALREADY_EXISTS);
    }

    @Test
    void 기수를_삭제한다() {
        // given
        Gisu gisu = Gisu.create(
                11L,
                Instant.parse("2025-03-01T00:00:00Z"),
                Instant.parse("2025-08-31T23:59:59Z"),
                false
        );
        Gisu savedGisu = manageGisuPort.save(gisu);
        Long gisuId = savedGisu.getId();

        // when
        manageGisuUseCase.deleteGisu(gisuId);

        // then
        assertThatThrownBy(() -> getGisuUseCase.getById(gisuId))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(OrganizationErrorCode.GISU_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_기수를_삭제하면_예외가_발생한다() {
        // given
        Long nonExistentGisuId = 9999L;

        // when & then
        assertThatThrownBy(() -> manageGisuUseCase.deleteGisu(nonExistentGisuId))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(OrganizationErrorCode.GISU_NOT_FOUND);
    }

    @Test
    void 활성_기수를_변경한다() {
        // given
        Gisu oldActiveGisu = Gisu.create(
                8L,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                true
        );
        oldActiveGisu = manageGisuPort.save(oldActiveGisu);

        Gisu newGisu = Gisu.create(
                9L,
                Instant.parse("2025-03-01T00:00:00Z"),
                Instant.parse("2025-08-31T23:59:59Z"),
                false
        );
        newGisu = manageGisuPort.save(newGisu);

        // when
        manageGisuUseCase.updateActiveGisu(newGisu.getId());

        // then
        GisuInfo oldGisuInfo = getGisuUseCase.getById(oldActiveGisu.getId());
        GisuInfo newGisuInfo = getGisuUseCase.getById(newGisu.getId());

        assertThat(oldGisuInfo.isActive()).isFalse();
        assertThat(newGisuInfo.isActive()).isTrue();
    }
}
