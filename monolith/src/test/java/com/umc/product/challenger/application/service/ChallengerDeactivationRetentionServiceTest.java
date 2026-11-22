package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationRetentionService")
class ChallengerDeactivationRetentionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T00:00:00Z");

    @Mock
    LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Mock
    DeleteChallengerDeactivationDataUseCase deleteChallengerDeactivationDataUseCase;

    ChallengerDeactivationRetentionService sut;

    @BeforeEach
    void setUp() {
        sut = new ChallengerDeactivationRetentionService(
            loadChallengerDeactivationPort,
            deleteChallengerDeactivationDataUseCase,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("등록일로부터 5년이 지난 탈부 기록을 삭제한다")
    void purgeExpiredDeactivationsAndExclusiveEvidenceFiles() {
        given(loadChallengerDeactivationPort.listRetentionTargets(any(), anyLong(), anyInt()))
            .willReturn(List.of(
                new ChallengerDeactivationRetentionTarget(1L),
                new ChallengerDeactivationRetentionTarget(2L)
            ));

        ChallengerDeactivationPurgeResult result = sut.purgeExpired();

        assertThat(result).isEqualTo(new ChallengerDeactivationPurgeResult(2, 0));
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        then(loadChallengerDeactivationPort).should()
            .listRetentionTargets(thresholdCaptor.capture(), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(100));
        assertThat(thresholdCaptor.getValue()).isEqualTo(Instant.parse("2021-11-16T00:00:00Z"));
        then(deleteChallengerDeactivationDataUseCase).should().delete(1L);
        then(deleteChallengerDeactivationDataUseCase).should().delete(2L);
    }

    @Test
    @DisplayName("한 기록 삭제에 실패해도 다음 만료 기록을 계속 처리한다")
    void continueAfterTargetFailure() {
        given(loadChallengerDeactivationPort.listRetentionTargets(any(), anyLong(), anyInt()))
            .willReturn(List.of(
                new ChallengerDeactivationRetentionTarget(1L),
                new ChallengerDeactivationRetentionTarget(2L)
            ));
        org.mockito.BDDMockito.willThrow(new IllegalStateException("스토리지 삭제 실패"))
            .given(deleteChallengerDeactivationDataUseCase).delete(1L);

        ChallengerDeactivationPurgeResult result = sut.purgeExpired();

        assertThat(result).isEqualTo(new ChallengerDeactivationPurgeResult(1, 1));
        then(deleteChallengerDeactivationDataUseCase).should().delete(2L);
    }

    @Test
    @DisplayName("100건 단위로 다음 배치를 조회하고 전체 성공과 실패 건수를 합산한다")
    void paginateAndAccumulateResult() {
        List<ChallengerDeactivationRetentionTarget> firstBatch = LongStream.rangeClosed(1, 100)
            .mapToObj(ChallengerDeactivationRetentionTarget::new)
            .toList();
        List<ChallengerDeactivationRetentionTarget> secondBatch = List.of(
            new ChallengerDeactivationRetentionTarget(101L),
            new ChallengerDeactivationRetentionTarget(102L)
        );
        given(loadChallengerDeactivationPort.listRetentionTargets(any(), anyLong(), anyInt()))
            .willReturn(firstBatch)
            .willReturn(secondBatch);
        org.mockito.Mockito.lenient()
            .doThrow(new IllegalStateException("스토리지 삭제 실패"))
            .when(deleteChallengerDeactivationDataUseCase).delete(101L);

        ChallengerDeactivationPurgeResult result = sut.purgeExpired();

        assertThat(result).isEqualTo(new ChallengerDeactivationPurgeResult(101, 1));
        then(loadChallengerDeactivationPort).should().listRetentionTargets(any(), eq(0L), eq(100));
        then(loadChallengerDeactivationPort).should().listRetentionTargets(any(), eq(100L), eq(100));
        then(loadChallengerDeactivationPort).should(times(2))
            .listRetentionTargets(any(), anyLong(), anyInt());
    }

}
