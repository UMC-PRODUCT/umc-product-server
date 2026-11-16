package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationHistoryJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationImageJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationLinkJpaRepository;
import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("탈부 보관 만료 삭제 트랜잭션")
class ChallengerDeactivationDeletionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    DeleteChallengerDeactivationDataUseCase deleteChallengerDeactivationDataUseCase;

    @Autowired
    SaveChallengerDeactivationPort saveChallengerDeactivationPort;

    @Autowired
    ChallengerDeactivationJpaRepository deactivationRepository;

    @Autowired
    ChallengerDeactivationHistoryJpaRepository historyRepository;

    @Autowired
    ChallengerDeactivationImageJpaRepository imageRepository;

    @Autowired
    ChallengerDeactivationLinkJpaRepository linkRepository;

    @MockitoSpyBean
    DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

    @Test
    @DisplayName("자식 기록 삭제 중 실패하면 탈부 기록과 모든 근거 자료를 복구한다")
    void rollbackAllDataWhenChildDeletionFails() {
        ChallengerDeactivation deactivation = saveChallengerDeactivationPort.save(
            ChallengerDeactivation.create(
                1L,
                ChallengerStatus.WITHDRAWN,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                null,
                null,
                10L
            )
        );
        ChallengerDeactivationHistory history = saveChallengerDeactivationPort.saveHistory(
            ChallengerDeactivationHistory.create(
                deactivation,
                ChallengerDeactivationHistoryAction.REGISTERED,
                1,
                1,
                10L
            )
        );
        saveChallengerDeactivationPort.saveImages(List.of(
            ChallengerDeactivationImage.create(history, "image-id", 0)
        ));
        saveChallengerDeactivationPort.saveLinks(List.of(
            ChallengerDeactivationLink.create(history, "https://example.com", 0)
        ));
        doThrow(new IllegalStateException("진행 기록 삭제 실패"))
            .when(deleteChallengerDeactivationPort)
            .deleteHistoriesByDeactivationId(deactivation.getId());

        assertThatThrownBy(() -> deleteChallengerDeactivationDataUseCase.delete(deactivation.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("진행 기록 삭제 실패");

        assertThat(deactivationRepository.existsById(deactivation.getId())).isTrue();
        assertThat(historyRepository.count()).isOne();
        assertThat(imageRepository.count()).isOne();
        assertThat(linkRepository.count()).isOne();
    }
}
