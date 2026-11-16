package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationHistoryJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationLinkJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerJpaRepository;
import com.umc.product.challenger.application.port.in.command.AddChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("탈부 등록 트랜잭션")
class ChallengerDeactivationRegistrationIntegrationTest extends IntegrationTestSupport {

    @Autowired private RegisterChallengerDeactivationUseCase registerDeactivation;
    @Autowired private AddChallengerDeactivationEvidenceUseCase addDeactivationEvidence;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private ChallengerJpaRepository challengerRepository;
    @Autowired private ChallengerDeactivationJpaRepository deactivationRepository;
    @Autowired private ChallengerDeactivationHistoryJpaRepository historyRepository;
    @Autowired private ChallengerDeactivationLinkJpaRepository linkRepository;
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

    @Test
    @DisplayName("근거 링크 저장에 실패하면 근거 추가 진행 기록도 롤백한다")
    void rollbackEvidenceHistoryWhenLinkSaveFails() {
        Challenger challenger = saveChallengerPort.save(Challenger.builder()
            .memberId(1L)
            .gisuId(11L)
            .part(ChallengerPart.SPRINGBOOT)
            .build());
        registerDeactivation.register(RegisterChallengerDeactivationCommand.of(
            challenger.getId(),
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            List.of(),
            List.of(),
            10L
        ));
        doThrow(new IllegalStateException("근거 링크 저장 실패"))
            .when(saveDeactivationPort).saveLinks(anyList());

        assertThatThrownBy(() -> addDeactivationEvidence.addEvidence(
            AddChallengerDeactivationEvidenceCommand.of(
                challenger.getId(),
                List.of(),
                List.of("https://example.com/evidence"),
                10L
            )
        )).isInstanceOf(IllegalStateException.class);

        assertThat(historyRepository.count()).isEqualTo(1L);
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    @DisplayName("동시에 근거 자료를 추가해도 비관적 락으로 링크 개수 제한을 지킨다")
    void keepEvidenceCapacityUnderConcurrentRequests() throws Exception {
        Challenger challenger = saveChallengerPort.save(Challenger.builder()
            .memberId(1L)
            .gisuId(11L)
            .part(ChallengerPart.SPRINGBOOT)
            .build());
        registerDeactivation.register(RegisterChallengerDeactivationCommand.of(
            challenger.getId(),
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            List.of(),
            List.of(),
            10L
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Throwable> first = executor.submit(() -> addLinksConcurrently(
                challenger.getId(),
                List.of("https://example.com/first-1", "https://example.com/first-2"),
                ready,
                start
            ));
            Future<Throwable> second = executor.submit(() -> addLinksConcurrently(
                challenger.getId(),
                List.of("https://example.com/second-1", "https://example.com/second-2"),
                ready,
                start
            ));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<Throwable> results = Arrays.asList(
                first.get(10, TimeUnit.SECONDS),
                second.get(10, TimeUnit.SECONDS)
            );

            assertThat(results).filteredOn(result -> result == null).hasSize(1);
            assertThat(results).filteredOn(result -> result instanceof ChallengerDomainException)
                .singleElement()
                .satisfies(result -> assertThat(((ChallengerDomainException) result).getBaseCode())
                    .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));
            assertThat(linkRepository.count()).isEqualTo(2L);
            assertThat(historyRepository.count()).isEqualTo(2L);
        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable addLinksConcurrently(
        Long challengerId,
        List<String> links,
        CountDownLatch ready,
        CountDownLatch start
    ) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                return new IllegalStateException("동시 요청 시작 대기 시간이 초과되었습니다.");
            }
            addDeactivationEvidence.addEvidence(AddChallengerDeactivationEvidenceCommand.of(
                challengerId,
                List.of(),
                links,
                10L
            ));
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }
}
