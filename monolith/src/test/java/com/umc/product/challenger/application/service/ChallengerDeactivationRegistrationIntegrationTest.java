package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationHistoryJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerJpaRepository;
import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("탈부 등록 트랜잭션")
class ChallengerDeactivationRegistrationIntegrationTest extends IntegrationTestSupport {

    @Autowired private RegisterChallengerDeactivationUseCase registerDeactivation;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private ChallengerJpaRepository challengerRepository;
    @Autowired private ChallengerDeactivationJpaRepository deactivationRepository;
    @Autowired private ChallengerDeactivationHistoryJpaRepository historyRepository;
    @MockitoSpyBean private SaveChallengerDeactivationPort saveDeactivationPort;

    @Test
    @DisplayName("진행 기록 저장에 실패하면 챌린저 상태와 탈부 기록을 모두 롤백한다")
    void rollbackAllChangesWhenHistorySaveFails() {
        Challenger challenger = saveChallengerPort.save(Challenger.builder()
            .memberId(1L)
            .gisuId(11L)
            .part(ChallengerPart.SPRINGBOOT)
            .build());
        doThrow(new IllegalStateException("진행 기록 저장 실패"))
            .when(saveDeactivationPort).saveHistory(any(ChallengerDeactivationHistory.class));
        RegisterChallengerDeactivationCommand command = RegisterChallengerDeactivationCommand.of(
            challenger.getId(),
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            "운영진 메모",
            List.of(),
            List.of(),
            10L
        );

        assertThatThrownBy(() -> registerDeactivation.register(command))
            .isInstanceOf(IllegalStateException.class);

        Challenger savedChallenger = challengerRepository.findById(challenger.getId()).orElseThrow();
        assertThat(savedChallenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        assertThat(savedChallenger.getModifiedBy()).isNull();
        assertThat(deactivationRepository.existsByChallengerId(challenger.getId())).isFalse();
        assertThat(historyRepository.count()).isZero();
    }
}
