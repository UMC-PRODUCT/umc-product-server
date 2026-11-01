package com.umc.product.report.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 전달 서비스")
class ReportDeliveryServiceTest {

    private static final Long REPORT_ID = 901L;
    private static final Instant NOW = Instant.parse("2026-10-26T09:00:00Z");
    private static final int MAX_ATTEMPTS = 2;

    @Mock
    LoadReportPort loadReportPort;

    @Mock
    SaveReportPort saveReportPort;

    @Mock
    ReportDeliveryPort reportDeliveryPort;

    ReportDeliveryService sut;

    @BeforeEach
    void setUp() {
        sut = new ReportDeliveryService(
            loadReportPort,
            saveReportPort,
            reportDeliveryPort,
            Clock.fixed(NOW, ZoneOffset.UTC),
            MAX_ATTEMPTS
        );
    }

    @Test
    @DisplayName("전달 대기 중인 신고를 보내고 SENT와 전달 시각을 저장한다")
    void deliver_성공하면_SENT로_저장한다() {
        // given
        given(reportDeliveryPort.isEnabled()).willReturn(true);
        Report report = pendingReport();
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.of(report));

        // when
        sut.deliver(REPORT_ID);

        // then
        then(reportDeliveryPort).should().deliver(report);
        then(saveReportPort).should().save(report);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.SENT);
        assertThat(report.getDeliveredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("전달에 실패하면 실패를 기록하고 예외를 다시 던져 재시도하게 한다")
    void deliver_실패하면_기록하고_예외를_다시_던진다() {
        // given
        given(reportDeliveryPort.isEnabled()).willReturn(true);
        Report report = pendingReport();
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.of(report));
        RuntimeException failure = new IllegalStateException("discord unavailable");
        willThrow(failure).given(reportDeliveryPort).deliver(report);

        // when & then
        assertThatThrownBy(() -> sut.deliver(REPORT_ID)).isSameAs(failure);
        then(saveReportPort).should().save(report);
        assertThat(report.getDeliveryAttemptCount()).isEqualTo(1);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
    }

    @Test
    @DisplayName("마지막 시도까지 실패하면 FAILED로 저장한다")
    void deliver_마지막_시도도_실패하면_FAILED가_된다() {
        // given
        given(reportDeliveryPort.isEnabled()).willReturn(true);
        Report report = pendingReport();
        report.recordDeliveryFailure(MAX_ATTEMPTS);
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.of(report));
        willThrow(new IllegalStateException("discord unavailable")).given(reportDeliveryPort).deliver(report);

        // when & then
        assertThatThrownBy(() -> sut.deliver(REPORT_ID)).isInstanceOf(IllegalStateException.class);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("운영 채널이 설정되지 않았으면 보내지 않고 전달 대기로 남긴다")
    void deliver_채널이_없으면_PENDING으로_남긴다() {
        // given
        Report report = pendingReport();
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.of(report));
        given(reportDeliveryPort.isEnabled()).willReturn(false);

        // when
        sut.deliver(REPORT_ID);

        // then
        then(reportDeliveryPort).should(never()).deliver(report);
        then(saveReportPort).shouldHaveNoInteractions();
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
    }

    @Test
    @DisplayName("이미 전달한 신고는 다시 보내지 않는다")
    void deliver_이미_전달했으면_건너뛴다() {
        // given
        Report report = pendingReport();
        report.markDelivered(NOW);
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.of(report));

        // when
        sut.deliver(REPORT_ID);

        // then
        then(reportDeliveryPort).shouldHaveNoInteractions();
        then(saveReportPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("신고가 없으면 아무것도 하지 않는다")
    void deliver_신고가_없으면_건너뛴다() {
        // given
        given(loadReportPort.findById(REPORT_ID)).willReturn(Optional.empty());

        // when
        sut.deliver(REPORT_ID);

        // then
        then(reportDeliveryPort).shouldHaveNoInteractions();
        then(saveReportPort).shouldHaveNoInteractions();
    }

    private Report pendingReport() {
        return Report.create(1L, ReportTargetType.POST, 2L, null);
    }
}
