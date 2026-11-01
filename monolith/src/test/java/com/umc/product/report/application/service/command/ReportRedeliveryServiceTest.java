package com.umc.product.report.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsCommand;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.event.ReportSubmittedEvent;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("미전달 신고 재전송 서비스")
class ReportRedeliveryServiceTest {

    private static final Long ADMIN_ID = 1L;
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");

    @Mock
    CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    @Mock
    LoadReportPort loadReportPort;

    @Mock
    SaveReportPort saveReportPort;

    @Mock
    ReportDeliveryPort reportDeliveryPort;

    @Mock
    DomainEventPublisher domainEventPublisher;

    ReportRedeliveryService sut;

    @BeforeEach
    void setUp() {
        sut = new ReportRedeliveryService(
            checkChallengerAuthorityUseCase,
            loadReportPort,
            saveReportPort,
            reportDeliveryPort,
            domainEventPublisher,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("접수 후 5분이 지난 재전송 대상 신고를 PENDING으로 되돌리고 전달 이벤트를 다시 넣는다")
    @SuppressWarnings("unchecked")
    void redeliver_미전달_신고를_다시_넣는다() {
        // given
        given(checkChallengerAuthorityUseCase.isSuperAdmin(ADMIN_ID)).willReturn(true);
        given(reportDeliveryPort.isEnabled()).willReturn(true);
        Report failed = report(901L);
        failed.recordDeliveryFailure(1);
        Report pending = report(902L);
        given(loadReportPort.findRedeliveryCandidates(NOW.minus(ReportRedeliveryService.IN_FLIGHT_GRACE), 50))
            .willReturn(List.of(failed, pending));

        // when
        RedeliverReportsResult result = sut.redeliver(new RedeliverReportsCommand(ADMIN_ID, 50));

        // then
        assertThat(result.reportIds()).containsExactly(901L, 902L);
        assertThat(failed.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(failed.getDeliveryAttemptCount()).isZero();
        then(saveReportPort).should().save(failed);
        then(saveReportPort).should().save(pending);

        ArgumentCaptor<Collection<? extends DomainEvent>> captor = ArgumentCaptor.forClass(Collection.class);
        then(domainEventPublisher).should().publishAll(captor.capture());
        List<DomainEvent> events = List.copyOf(captor.getValue());
        assertThat(events)
            .extracting(event -> ((ReportSubmittedEvent) event).reportId())
            .containsExactly(901L, 902L);
    }

    @Test
    @DisplayName("SUPER_ADMIN이 아니면 REPORT_ADMIN_REQUIRED를 던지고 아무것도 넣지 않는다")
    void redeliver_관리자가_아니면_거부한다() {
        // given
        given(checkChallengerAuthorityUseCase.isSuperAdmin(2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.redeliver(new RedeliverReportsCommand(2L, 100)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ADMIN_REQUIRED)
            );
        assertThatThrownBy(() -> sut.redeliver(new RedeliverReportsCommand(null, 100)))
            .isInstanceOf(ReportDomainException.class);
        then(loadReportPort).should(never()).findRedeliveryCandidates(any(), anyInt());
        then(domainEventPublisher).should(never()).publishAll(any());
    }

    @Test
    @DisplayName("운영 채널이 설정되지 않았으면 REPORT_DELIVERY_DISABLED를 던진다")
    void redeliver_운영_채널이_없으면_거부한다() {
        // given
        given(checkChallengerAuthorityUseCase.isSuperAdmin(ADMIN_ID)).willReturn(true);
        given(reportDeliveryPort.isEnabled()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.redeliver(new RedeliverReportsCommand(ADMIN_ID, 100)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_DELIVERY_DISABLED)
            );
        then(loadReportPort).should(never()).findRedeliveryCandidates(any(), anyInt());
    }

    @Test
    @DisplayName("한 번에 다시 보낼 수는 1건부터 100건까지다")
    void command_limit_범위() {
        assertThatThrownBy(() -> new RedeliverReportsCommand(ADMIN_ID, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RedeliverReportsCommand(ADMIN_ID, 101))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private Report report(Long id) {
        Report report = Report.create(10L, ReportTargetType.POST, id, null);
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }
}
