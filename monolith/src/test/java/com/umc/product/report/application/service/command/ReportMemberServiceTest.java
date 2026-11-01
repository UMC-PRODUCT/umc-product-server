package com.umc.product.report.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.ReportMemberCommand;
import com.umc.product.report.application.port.in.command.dto.ReportMemberResult;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 신고 명령 서비스")
class ReportMemberServiceTest {

    private static final Long REQUESTER_ID = 44L;
    private static final Long TARGET_ID = 55L;
    private static final Instant CREATED_AT = Instant.parse("2026-11-01T00:00:00Z");

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    SubmitReportUseCase submitReportUseCase;

    @InjectMocks
    ReportMemberService sut;

    @Test
    @DisplayName("대상 회원이 있으면 신고 당시 닉네임을 스냅샷으로 남겨 report에 넘기고 접수 정보를 돌려준다")
    void report_닉네임_스냅샷으로_신고를_접수한다() {
        // given
        given(getMemberUseCase.getById(TARGET_ID)).willReturn(member("대상닉네임"));
        given(submitReportUseCase.submit(any(SubmitReportCommand.class)))
            .willReturn(new SubmitReportResult(901L, List.of(ReportReason.HARASSMENT, ReportReason.SPAM), CREATED_AT));

        // when
        ReportMemberResult receipt = sut.report(
            new ReportMemberCommand(TARGET_ID, REQUESTER_ID, List.of(ReportReason.HARASSMENT, ReportReason.SPAM), "상세 내용")
        );

        // then
        assertThat(receipt.reportId()).isEqualTo(901L);
        assertThat(receipt.memberId()).isEqualTo(TARGET_ID);
        assertThat(receipt.reasons()).containsExactly(ReportReason.HARASSMENT, ReportReason.SPAM);
        assertThat(receipt.createdAt()).isEqualTo(CREATED_AT);

        ArgumentCaptor<SubmitReportCommand> captor = ArgumentCaptor.forClass(SubmitReportCommand.class);
        then(submitReportUseCase).should().submit(captor.capture());
        SubmitReportCommand submitted = captor.getValue();
        assertThat(submitted.reporterId()).isEqualTo(REQUESTER_ID);
        assertThat(submitted.targetType()).isEqualTo(ReportTargetType.USER);
        assertThat(submitted.targetId()).isEqualTo(TARGET_ID);
        assertThat(submitted.threadId()).isNull();
        assertThat(submitted.reasons()).containsExactly(ReportReason.HARASSMENT, ReportReason.SPAM);
        assertThat(submitted.detail()).isEqualTo("상세 내용");
        assertThat(submitted.targetSnapshot().authorId()).isEqualTo(TARGET_ID);
        assertThat(submitted.targetSnapshot().content()).isNull();
        assertThat(submitted.targetSnapshot().context()).isEqualTo("대상닉네임");
    }

    @Test
    @DisplayName("대상 회원이 없으면 MEMBER_NOT_FOUND를 그대로 전달하고 신고를 접수하지 않는다")
    void report_대상_회원이_없으면_거부한다() {
        // given
        given(getMemberUseCase.getById(TARGET_ID))
            .willThrow(new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.report(
            new ReportMemberCommand(TARGET_ID, REQUESTER_ID, List.of(ReportReason.SPAM), "상세")
        )).isInstanceOfSatisfying(MemberDomainException.class, exception ->
            assertThat(exception.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
        );
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("자기 자신을 신고하면 REPORT_SELF_REPORT로 거부하고 회원 조회와 접수를 하지 않는다")
    void report_자기_자신_신고를_거부한다() {
        // when & then
        assertThatThrownBy(() -> sut.report(
            new ReportMemberCommand(REQUESTER_ID, REQUESTER_ID, List.of(ReportReason.SPAM), "상세")
        )).isInstanceOfSatisfying(ReportDomainException.class, exception ->
            assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_SELF_REPORT)
        );
        then(getMemberUseCase).shouldHaveNoInteractions();
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 신고한 사용자면 REPORT_ALREADY_EXISTS를 그대로 전달한다")
    void report_중복_신고를_거부한다() {
        // given
        given(getMemberUseCase.getById(TARGET_ID)).willReturn(member("대상닉네임"));
        given(submitReportUseCase.submit(any(SubmitReportCommand.class)))
            .willThrow(new ReportDomainException(ReportErrorCode.REPORT_ALREADY_EXISTS));

        // when & then
        assertThatThrownBy(() -> sut.report(
            new ReportMemberCommand(TARGET_ID, REQUESTER_ID, List.of(ReportReason.SPAM), "상세")
        )).isInstanceOfSatisfying(ReportDomainException.class, exception ->
            assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS)
        );
    }

    private MemberInfo member(String nickname) {
        return MemberInfo.builder().id(TARGET_ID).nickname(nickname).build();
    }
}
