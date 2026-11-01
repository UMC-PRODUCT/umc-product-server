package com.umc.product.community.application.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.chat.application.port.in.query.GetChatMessageRoomUseCase;
import com.umc.product.chat.application.port.in.query.GetChatMessageUseCase;
import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessageQuery;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessageRoomQuery;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.community.application.port.in.command.thread.report.dto.ReportCommunityThreadMessageCommand;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageReportReceiptInfo;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadMemberPort;
import com.umc.product.community.application.port.out.thread.LoadCommunityThreadPort;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.CommunityThreadMember;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("스레드 메시지 신고 명령 서비스")
class CommunityThreadMessageReportCommandServiceTest {

    private static final Long THREAD_ID = 11L;
    private static final Long CHAT_ROOM_ID = 22L;
    private static final Long MESSAGE_ID = 33L;
    private static final Long REQUESTER_MEMBER_ID = 44L;
    private static final Instant CREATED_AT = Instant.parse("2026-07-18T00:00:00Z");

    @Mock
    LoadCommunityThreadPort loadCommunityThreadPort;

    @Mock
    LoadCommunityThreadMemberPort loadCommunityThreadMemberPort;

    @Mock
    GetChatMessageRoomUseCase getChatMessageRoomUseCase;

    @Mock
    GetChatMessageUseCase getChatMessageUseCase;

    @Mock
    SubmitReportUseCase submitReportUseCase;

    @InjectMocks
    CommunityThreadMessageReportCommandService sut;

    @Test
    @DisplayName("Challenger 기록이 없어도 ACTIVE 회원은 자신의 member ID로 메시지를 신고한다")
    void report_Challenger_기록이_없는_활성_회원도_member_ID로_신고한다() {
        // given
        CommunityThread thread = thread();
        given(thread.getTitle()).willReturn("스레드 제목");
        CommunityThreadMember member = activeMember();
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.of(member));
        given(getChatMessageUseCase.getMessage(
            new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID)
        )).willReturn(chatMessage());
        given(submitReportUseCase.submit(any(SubmitReportCommand.class)))
            .willReturn(new SubmitReportResult(901L, List.of(ReportReason.ABUSE), CREATED_AT));

        // when
        CommunityThreadMessageReportReceiptInfo result = sut.report(command());

        // then
        assertThat(result.reportId()).isEqualTo(901L);
        assertThat(result.messageId()).isEqualTo(MESSAGE_ID);
        assertThat(result.reason()).isEqualTo(ReportReason.ABUSE);
        assertThat(result.reasons()).containsExactly(ReportReason.ABUSE);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(Arrays.stream(CommunityThreadMessageReportReceiptInfo.class.getRecordComponents())
            .map(RecordComponent::getName)
            .toList())
            .doesNotContain("reporterId", "reporterMemberId");

        ArgumentCaptor<SubmitReportCommand> submitCaptor = ArgumentCaptor.forClass(SubmitReportCommand.class);
        then(submitReportUseCase).should().submit(submitCaptor.capture());
        SubmitReportCommand submitted = submitCaptor.getValue();
        assertThat(submitted.reporterId()).isEqualTo(REQUESTER_MEMBER_ID);
        assertThat(submitted.targetType()).isEqualTo(ReportTargetType.THREAD_MESSAGE);
        assertThat(submitted.threadId()).isEqualTo(THREAD_ID);
        assertThat(submitted.targetId()).isEqualTo(MESSAGE_ID);
        assertThat(submitted.reasons()).containsExactly(ReportReason.ABUSE);
        assertThat(submitted.targetSnapshot())
            .isEqualTo(new SubmitReportCommand.TargetSnapshot(66L, "신고 대상 메시지", "스레드 제목"));

        InOrder order = inOrder(
            getChatMessageRoomUseCase,
            loadCommunityThreadPort,
            loadCommunityThreadMemberPort,
            getChatMessageUseCase,
            submitReportUseCase
        );
        order.verify(getChatMessageRoomUseCase)
            .getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID));
        order.verify(loadCommunityThreadPort).findByChatRoomId(CHAT_ROOM_ID);
        order.verify(loadCommunityThreadPort).findByIdForUpdate(THREAD_ID);
        order.verify(loadCommunityThreadMemberPort)
            .findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID);
        order.verify(getChatMessageUseCase)
            .getMessage(new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID));
        order.verify(submitReportUseCase).submit(any(SubmitReportCommand.class));
    }

    @Test
    @DisplayName("메시지 room에 매핑된 스레드가 없으면 THREAD_NOT_FOUND로 종료한다")
    void report_스레드가_없으면_신고하지_않는다() {
        // given
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_NOT_FOUND);

        verifyNoInteractions(
            loadCommunityThreadMemberPort,
            getChatMessageUseCase,
            submitReportUseCase
        );
    }

    @Test
    @DisplayName("room reverse mapping 뒤 잠금 스레드의 chatRoomId가 다르면 멤버 검증 전에 THREAD_NOT_FOUND로 종료한다")
    void report_잠금_스레드_room이_다르면_접근할수없다() {
        // given
        CommunityThread mappedThread = mock(CommunityThread.class);
        given(mappedThread.getId()).willReturn(THREAD_ID);
        CommunityThread lockedThread = mock(CommunityThread.class);
        given(lockedThread.getChatRoomId()).willReturn(99L);
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(mappedThread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(lockedThread));

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_NOT_FOUND);

        then(loadCommunityThreadMemberPort).shouldHaveNoInteractions();
        then(getChatMessageUseCase).shouldHaveNoInteractions();
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("room reverse mapping 뒤 잠금 스레드가 삭제되면 멤버 검증 전에 THREAD_NOT_FOUND로 종료한다")
    void report_잠금_스레드가_삭제되면_접근할수없다() {
        // given
        CommunityThread mappedThread = mock(CommunityThread.class);
        given(mappedThread.getId()).willReturn(THREAD_ID);
        CommunityThread lockedThread = mock(CommunityThread.class);
        given(lockedThread.getChatRoomId()).willReturn(CHAT_ROOM_ID);
        given(lockedThread.isDeleted()).willReturn(true);
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(mappedThread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(lockedThread));

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_NOT_FOUND);

        then(loadCommunityThreadMemberPort).shouldHaveNoInteractions();
        then(getChatMessageUseCase).shouldHaveNoInteractions();
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원이 없으면 메시지/신고 포트를 호출하지 않고 THREAD_MEMBER_NOT_FOUND로 종료한다")
    void report_스레드_회원이_없으면_신고하지_않는다() {
        // given
        CommunityThread thread = thread();
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_MEMBER_NOT_FOUND);

        then(getChatMessageUseCase).shouldHaveNoInteractions();
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("LEFT 또는 KICKED 회원은 Chat 단건 조회 전에 THREAD_ACCESS_DENIED로 차단한다")
    void report_비활성_회원은_접근이_거부된다() {
        // given
        CommunityThread thread = thread();
        CommunityThreadMember inactiveMember = mock(CommunityThreadMember.class);
        given(inactiveMember.isActive()).willReturn(false);
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.of(inactiveMember));

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_ACCESS_DENIED);

        then(getChatMessageUseCase).shouldHaveNoInteractions();
        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("본인이 보낸 메시지는 중복 확인과 저장 전에 REPORT_SELF_TARGET으로 거부한다")
    void report_본인_메시지_신고를_거부한다() {
        // given
        CommunityThread thread = thread();
        CommunityThreadMember member = activeMember();
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.of(member));
        given(getChatMessageUseCase.getMessage(
            new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID)
        )).willReturn(chatMessageFrom(REQUESTER_MEMBER_ID));

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isInstanceOf(ReportDomainException.class)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(ReportErrorCode.REPORT_SELF_TARGET);

        then(submitReportUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("신고 엔진의 중복 신고 거부는 Chat 검증 뒤 그대로 전달한다")
    void report_중복_신고_거부를_그대로_전달한다() {
        // given
        CommunityThread thread = thread();
        CommunityThreadMember member = activeMember();
        ReportDomainException duplicate = new ReportDomainException(ReportErrorCode.REPORT_ALREADY_EXISTS);
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.of(member));
        given(getChatMessageUseCase.getMessage(
            new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID)
        )).willReturn(chatMessage());
        given(submitReportUseCase.submit(any(SubmitReportCommand.class))).willThrow(duplicate);

        // when & then
        assertThatThrownBy(() -> sut.report(command()))
            .isSameAs(duplicate)
            .extracting(error -> ((BusinessException) error).getBaseCode())
            .isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS);

        InOrder order = inOrder(getChatMessageUseCase, submitReportUseCase);
        order.verify(getChatMessageUseCase)
            .getMessage(new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID));
        order.verify(submitReportUseCase).submit(any(SubmitReportCommand.class));
    }

    @Test
    @DisplayName("본문이 없는 이미지 메시지는 콘텐츠 타입으로 스냅샷 내용을 남긴다")
    void report_본문_없는_이미지는_타입으로_스냅샷을_남긴다() {
        // given
        stubReportable(new ChatMessageInfo(
            MESSAGE_ID, CHAT_ROOM_ID, 66L, MessageContentType.IMAGE, null, List.of(), CREATED_AT
        ));

        // when
        sut.report(command());

        // then
        assertThat(submittedSnapshot())
            .isEqualTo(new SubmitReportCommand.TargetSnapshot(66L, "[IMAGE]", "스레드 제목"));
    }

    @Test
    @DisplayName("발신자가 없는 시스템 메시지도 작성자 없이 신고를 접수한다")
    void report_시스템_메시지는_작성자_없이_접수한다() {
        // given
        stubReportable(new ChatMessageInfo(
            MESSAGE_ID, CHAT_ROOM_ID, null, MessageContentType.SYSTEM, "입장했습니다.", List.of(), CREATED_AT
        ));

        // when
        sut.report(command());

        // then
        assertThat(submittedSnapshot())
            .isEqualTo(new SubmitReportCommand.TargetSnapshot(null, "입장했습니다.", "스레드 제목"));
    }

    private void stubReportable(ChatMessageInfo message) {
        CommunityThread thread = thread();
        given(thread.getTitle()).willReturn("스레드 제목");
        CommunityThreadMember member = activeMember();
        given(getChatMessageRoomUseCase.getRoomId(new GetChatMessageRoomQuery(MESSAGE_ID)))
            .willReturn(CHAT_ROOM_ID);
        given(loadCommunityThreadPort.findByChatRoomId(CHAT_ROOM_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadPort.findByIdForUpdate(THREAD_ID)).willReturn(Optional.of(thread));
        given(loadCommunityThreadMemberPort.findByThreadIdAndMemberId(THREAD_ID, REQUESTER_MEMBER_ID))
            .willReturn(Optional.of(member));
        given(getChatMessageUseCase.getMessage(
            new GetChatMessageQuery(CHAT_ROOM_ID, REQUESTER_MEMBER_ID, MESSAGE_ID)
        )).willReturn(message);
        given(submitReportUseCase.submit(any(SubmitReportCommand.class)))
            .willReturn(new SubmitReportResult(901L, List.of(ReportReason.ABUSE), CREATED_AT));
    }

    private SubmitReportCommand.TargetSnapshot submittedSnapshot() {
        ArgumentCaptor<SubmitReportCommand> captor = ArgumentCaptor.forClass(SubmitReportCommand.class);
        then(submitReportUseCase).should().submit(captor.capture());
        return captor.getValue().targetSnapshot();
    }

    private ReportCommunityThreadMessageCommand command() {
        return new ReportCommunityThreadMessageCommand(
            MESSAGE_ID,
            REQUESTER_MEMBER_ID,
            List.of(ReportReason.ABUSE)
        );
    }

    private CommunityThread thread() {
        CommunityThread thread = mock(CommunityThread.class);
        given(thread.getId()).willReturn(THREAD_ID);
        given(thread.getChatRoomId()).willReturn(CHAT_ROOM_ID);
        given(thread.isDeleted()).willReturn(false);
        return thread;
    }

    private CommunityThreadMember activeMember() {
        CommunityThreadMember member = mock(CommunityThreadMember.class);
        given(member.isActive()).willReturn(true);
        return member;
    }

    private ChatMessageInfo chatMessage() {
        return chatMessageFrom(66L);
    }

    private ChatMessageInfo chatMessageFrom(Long senderMemberId) {
        return new ChatMessageInfo(
            MESSAGE_ID,
            CHAT_ROOM_ID,
            senderMemberId,
            MessageContentType.TEXT,
            "신고 대상 메시지",
            List.of(),
            CREATED_AT
        );
    }

}
