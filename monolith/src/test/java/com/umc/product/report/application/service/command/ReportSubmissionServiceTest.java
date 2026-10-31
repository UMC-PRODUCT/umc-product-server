package com.umc.product.report.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.event.ReportSubmittedEvent;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 접수 서비스")
class ReportSubmissionServiceTest {

    private static final Long REPORTER_ID = 1L;
    private static final Long THREAD_ID = 11L;
    private static final Long MESSAGE_ID = 33L;
    private static final Instant CREATED_AT = Instant.parse("2026-07-18T00:00:00Z");
    private static final SubmitReportCommand.TargetSnapshot SNAPSHOT =
        new SubmitReportCommand.TargetSnapshot(4L, "신고 대상 메시지", "스레드 제목");

    @Mock
    LoadReportPort loadReportPort;

    @Mock
    SaveReportPort saveReportPort;

    @Mock
    DomainEventPublisher domainEventPublisher;

    @InjectMocks
    ReportSubmissionService sut;

    @Test
    @DisplayName("스레드 메시지 신고는 스레드 ID와 사유 코드를 담아 저장하고 접수 정보를 돌려준다")
    void submit_스레드_메시지_신고를_저장한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
            REPORTER_ID, ReportTargetType.THREAD_MESSAGE, MESSAGE_ID
        )).willReturn(false);
        Report persisted = persisted(901L);
        given(saveReportPort.save(any(Report.class))).willReturn(persisted);

        // when
        SubmitReportResult result = sut.submit(
            SubmitReportCommand.threadMessage(REPORTER_ID, THREAD_ID, MESSAGE_ID, ReportReason.ABUSE, SNAPSHOT)
        );

        // then
        assertThat(result.reportId()).isEqualTo(901L);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);

        Report saved = capturedReport();
        assertThat(saved.getReporterId()).isEqualTo(REPORTER_ID);
        assertThat(saved.getTargetType()).isEqualTo(ReportTargetType.THREAD_MESSAGE);
        assertThat(saved.getTargetId()).isEqualTo(MESSAGE_ID);
        assertThat(saved.getThreadId()).isEqualTo(THREAD_ID);
        assertThat(saved.getReasonCode()).isEqualTo(ReportReason.ABUSE);
        assertThat(saved.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(saved.getTargetSnapshot().getAuthorId()).isEqualTo(4L);
        assertThat(saved.getTargetSnapshot().getContent()).isEqualTo("신고 대상 메시지");
        assertThat(saved.getTargetSnapshot().getContext()).isEqualTo("스레드 제목");
        assertThat(publishedReportId()).isEqualTo(901L);
    }

    @Test
    @DisplayName("게시글 신고는 스레드 ID와 사유 코드 없이 저장한다")
    void submit_게시글_신고를_저장한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(REPORTER_ID, ReportTargetType.POST, 7L))
            .willReturn(false);
        Report persisted = persisted(902L);
        given(saveReportPort.save(any(Report.class))).willReturn(persisted);

        // when
        sut.submit(SubmitReportCommand.of(REPORTER_ID, ReportTargetType.POST, 7L));

        // then
        Report saved = capturedReport();
        assertThat(saved.getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(saved.getTargetId()).isEqualTo(7L);
        assertThat(saved.getThreadId()).isNull();
        assertThat(saved.getReasonCode()).isNull();
        assertThat(publishedReportId()).isEqualTo(902L);
    }

    @Test
    @DisplayName("같은 신고자가 같은 대상을 이미 신고했으면 저장하지 않고 REPORT_ALREADY_EXISTS로 거부한다")
    void submit_중복_신고를_거부한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
            REPORTER_ID, ReportTargetType.THREAD_MESSAGE, MESSAGE_ID
        )).willReturn(true);

        // when & then
        assertThatThrownBy(() -> sut.submit(
            SubmitReportCommand.threadMessage(REPORTER_ID, THREAD_ID, MESSAGE_ID, ReportReason.ABUSE, SNAPSHOT)
        ))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS)
            );
        then(saveReportPort).shouldHaveNoInteractions();
        then(domainEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사전 확인과 저장 사이의 동시 중복 충돌은 그대로 전달한다")
    void submit_저장_시점_동시_중복_충돌을_그대로_전달한다() {
        // given
        ReportDomainException duplicate = new ReportDomainException(ReportErrorCode.REPORT_ALREADY_EXISTS);
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
            REPORTER_ID, ReportTargetType.THREAD_MESSAGE, MESSAGE_ID
        )).willReturn(false);
        given(saveReportPort.save(any(Report.class))).willThrow(duplicate);

        // when & then
        assertThatThrownBy(() -> sut.submit(
            SubmitReportCommand.threadMessage(REPORTER_ID, THREAD_ID, MESSAGE_ID, ReportReason.ABUSE, SNAPSHOT)
        )).isSameAs(duplicate);
        then(domainEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사용자 신고는 상세 내용과 닉네임 스냅샷을 담아 저장한다")
    void submit_사용자_신고를_저장한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(REPORTER_ID, ReportTargetType.USER, 8L))
            .willReturn(false);
        Report persisted = persisted(903L);
        given(saveReportPort.save(any(Report.class))).willReturn(persisted);

        // when
        SubmitReportResult result = sut.submit(SubmitReportCommand.user(
            REPORTER_ID, 8L, ReportReason.IMPERSONATION, " 다른 사람 사진을 써요 ",
            new SubmitReportCommand.TargetSnapshot(8L, null, "대상닉네임")
        ));

        // then
        assertThat(result.reportId()).isEqualTo(903L);
        Report saved = capturedReport();
        assertThat(saved.getTargetType()).isEqualTo(ReportTargetType.USER);
        assertThat(saved.getTargetId()).isEqualTo(8L);
        assertThat(saved.getThreadId()).isNull();
        assertThat(saved.getReasonCode()).isEqualTo(ReportReason.IMPERSONATION);
        assertThat(saved.getReason()).isEqualTo("다른 사람 사진을 써요");
        assertThat(saved.getTargetSnapshot().getAuthorId()).isEqualTo(8L);
        assertThat(saved.getTargetSnapshot().getContent()).isNull();
        assertThat(saved.getTargetSnapshot().getContext()).isEqualTo("대상닉네임");
        assertThat(publishedReportId()).isEqualTo(903L);
    }

    @Test
    @DisplayName("같은 신고자가 같은 사용자를 다시 신고하면 REPORT_ALREADY_EXISTS로 거부한다")
    void submit_사용자_중복_신고를_거부한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(REPORTER_ID, ReportTargetType.USER, 8L))
            .willReturn(true);

        // when & then
        assertThatThrownBy(() -> sut.submit(
            SubmitReportCommand.user(REPORTER_ID, 8L, ReportReason.SPAM, "상세", null)
        ))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS)
            );
        then(saveReportPort).shouldHaveNoInteractions();
        then(domainEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("자기 자신을 신고하면 REPORT_SELF_REPORT로 거부하고 저장하지 않는다")
    void submit_자기_자신_신고를_거부한다() {
        // given
        given(loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
            REPORTER_ID, ReportTargetType.USER, REPORTER_ID
        )).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.submit(
            SubmitReportCommand.user(REPORTER_ID, REPORTER_ID, ReportReason.SPAM, "상세", null)
        ))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_SELF_REPORT)
            );
        then(saveReportPort).shouldHaveNoInteractions();
        then(domainEventPublisher).shouldHaveNoInteractions();
    }

    private Report persisted(Long id) {
        Report report = mock(Report.class);
        given(report.getId()).willReturn(id);
        given(report.getCreatedAt()).willReturn(CREATED_AT);
        return report;
    }

    private Report capturedReport() {
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        then(saveReportPort).should().save(captor.capture());
        return captor.getValue();
    }

    private Long publishedReportId() {
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        then(domainEventPublisher).should().publish(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ReportSubmittedEvent.class);
        return ((ReportSubmittedEvent) captor.getValue()).reportId();
    }
}
