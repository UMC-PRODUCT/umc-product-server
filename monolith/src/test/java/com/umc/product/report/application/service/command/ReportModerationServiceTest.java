package com.umc.product.report.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.report.application.port.in.command.dto.ModerateReportCommand;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.SaveReportActionPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 조치 서비스")
class ReportModerationServiceTest {

    private static final Long REPORT_ID = 901L;
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");

    @Mock
    LoadReportPort loadReportPort;

    @Mock
    SaveReportPort saveReportPort;

    @Mock
    SaveReportActionPort saveReportActionPort;

    ReportModerationService sut;

    @BeforeEach
    void setUp() {
        sut = new ReportModerationService(
            loadReportPort,
            saveReportPort,
            saveReportActionPort,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("잠근 신고에 조치를 적용하고 상태와 조치 이력을 함께 저장한다")
    void moderate_상태와_이력을_저장한다() {
        // given
        Report report = pendingReport();
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));
        given(saveReportActionPort.save(any(ReportAction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ModerateReportResult result = sut.moderate(command(ReportActionType.REJECT));

        // then
        then(saveReportPort).should().save(report);
        ArgumentCaptor<ReportAction> actionCaptor = ArgumentCaptor.forClass(ReportAction.class);
        then(saveReportActionPort).should().save(actionCaptor.capture());
        ReportAction action = actionCaptor.getValue();
        assertThat(action.getReportId()).isEqualTo(REPORT_ID);
        assertThat(action.getActedAt()).isEqualTo(NOW);
        assertThat(result.reportId()).isEqualTo(REPORT_ID);
        assertThat(result.status()).isEqualTo(ReportStatus.REJECTED);
        assertThat(result.actionType()).isEqualTo(ReportActionType.REJECT);
        assertThat(result.reason()).isEqualTo("신고 사유에 해당하지 않음");
        assertThat(result.moderatorName()).isEqualTo("모루");
        assertThat(result.actedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("신고가 없으면 REPORT_NOT_FOUND를 던진다")
    void moderate_신고가_없으면_예외() {
        // given
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.moderate(command(ReportActionType.APPROVE)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_NOT_FOUND)
            );
        then(saveReportActionPort).should(never()).save(any());
    }

    @Test
    @DisplayName("이미 처리된 신고면 REPORT_ALREADY_MODERATED를 던지고 아무것도 저장하지 않는다")
    void moderate_이미_처리된_신고면_예외() {
        // given
        Report report = pendingReport();
        report.moderate(ReportActionType.APPROVE, "먼저 처리", "1", "다른 운영자", NOW);
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));

        // when & then
        assertThatThrownBy(() -> sut.moderate(command(ReportActionType.REJECT)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_MODERATED)
            );
        then(saveReportPort).should(never()).save(any());
        then(saveReportActionPort).should(never()).save(any());
    }

    private ModerateReportCommand command(ReportActionType actionType) {
        return new ModerateReportCommand(REPORT_ID, actionType, "신고 사유에 해당하지 않음", "1234567890", "모루");
    }

    private Report pendingReport() {
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);
        ReflectionTestUtils.setField(report, "id", REPORT_ID);
        return report;
    }
}
