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
import com.umc.product.report.application.port.out.RemoveReportTargetPort;
import com.umc.product.report.application.port.out.SaveReportActionPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 조치 서비스")
class ReportModerationServiceTest {

    private static final Long REPORT_ID = 901L;
    private static final Long THREAD_ID = 10L;
    private static final Long MESSAGE_ID = 20L;
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");

    @Mock
    LoadReportPort loadReportPort;

    @Mock
    SaveReportPort saveReportPort;

    @Mock
    SaveReportActionPort saveReportActionPort;

    @Mock
    RemoveReportTargetPort removeReportTargetPort;

    ReportModerationService sut;

    @BeforeEach
    void setUp() {
        sut = new ReportModerationService(
            loadReportPort,
            saveReportPort,
            saveReportActionPort,
            removeReportTargetPort,
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
        assertThat(action.getReasonCode()).isEqualTo(ReportActionReason.NOT_VIOLATION);
        assertThat(result.reasonCode()).isEqualTo(ReportActionReason.NOT_VIOLATION);
        assertThat(result.memo()).isEqualTo("맥락상 농담");
        assertThat(result.moderatorName()).isEqualTo("모루");
        assertThat(result.actedAt()).isEqualTo(NOW);
        assertThat(result.targetRemoval()).isEqualTo(ReportTargetRemoval.NONE);
    }

    @Test
    @DisplayName("스레드 메시지 신고를 처리하면 스레드와 메시지로 삭제를 요청하고 결과를 이력에 남긴다")
    void moderate_스레드_메시지_처리는_삭제한다() {
        // given
        Report report = threadMessageReport();
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));
        given(removeReportTargetPort.removeThreadMessage(THREAD_ID, MESSAGE_ID))
            .willReturn(ReportTargetRemoval.DELETED);
        given(saveReportActionPort.save(any(ReportAction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ModerateReportResult result = sut.moderate(command(ReportActionType.APPROVE));

        // then
        then(removeReportTargetPort).should().removeThreadMessage(THREAD_ID, MESSAGE_ID);
        assertThat(result.targetRemoval()).isEqualTo(ReportTargetRemoval.DELETED);
        assertThat(result.status()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("스레드 메시지 신고를 반려하면 삭제하지 않는다")
    void moderate_반려는_삭제하지_않는다() {
        // given
        Report report = threadMessageReport();
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));
        given(saveReportActionPort.save(any(ReportAction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ModerateReportResult result = sut.moderate(command(ReportActionType.REJECT));

        // then
        then(removeReportTargetPort).shouldHaveNoInteractions();
        assertThat(result.targetRemoval()).isEqualTo(ReportTargetRemoval.NONE);
    }

    @Test
    @DisplayName("게시글 신고를 처리해도 삭제하지 않는다")
    void moderate_게시글_신고는_삭제하지_않는다() {
        // given
        Report report = pendingReport();
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));
        given(saveReportActionPort.save(any(ReportAction.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ModerateReportResult result = sut.moderate(command(ReportActionType.APPROVE));

        // then
        then(removeReportTargetPort).shouldHaveNoInteractions();
        assertThat(result.targetRemoval()).isEqualTo(ReportTargetRemoval.NONE);
    }

    @Test
    @DisplayName("이미 처리된 스레드 메시지 신고는 메시지를 삭제하지 않고 거부한다")
    void moderate_이미_처리된_신고는_삭제하지_않는다() {
        // given
        Report report = threadMessageReport();
        report.moderate(
            ReportActionType.REJECT, ReportActionReason.NOT_VIOLATION, null, ReportTargetRemoval.NONE, "1",
            "다른 운영자", NOW
        );
        given(loadReportPort.findByIdForUpdate(REPORT_ID)).willReturn(Optional.of(report));

        // when & then
        assertThatThrownBy(() -> sut.moderate(command(ReportActionType.APPROVE)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_MODERATED)
            );
        then(removeReportTargetPort).shouldHaveNoInteractions();
        then(saveReportActionPort).should(never()).save(any());
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
        report.moderate(
            ReportActionType.APPROVE, ReportActionReason.ABUSE, null, ReportTargetRemoval.NONE, "1", "다른 운영자",
            NOW
        );
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
        ReportActionReason reason = actionType == ReportActionType.APPROVE
            ? ReportActionReason.ABUSE
            : ReportActionReason.NOT_VIOLATION;
        return new ModerateReportCommand(REPORT_ID, actionType, reason, "맥락상 농담", "1234567890", "모루");
    }

    private Report threadMessageReport() {
        Report report = Report.createThreadMessage(
            1L, THREAD_ID, MESSAGE_ID, ReportReason.ABUSE, ReportTargetSnapshot.of(4L, "신고 대상 메시지", "스레드 제목")
        );
        ReflectionTestUtils.setField(report, "id", REPORT_ID);
        return report;
    }

    private Report pendingReport() {
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);
        ReflectionTestUtils.setField(report, "id", REPORT_ID);
        return report;
    }
}
