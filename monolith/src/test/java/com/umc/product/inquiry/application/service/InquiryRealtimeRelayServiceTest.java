package com.umc.product.inquiry.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.inOrder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("문의 실시간 전달")
class InquiryRealtimeRelayServiceTest {

    private static final Long INQUIRY_ID = 10L;
    private static final Long ROOM_ID = 20L;
    private static final Long AUTHOR_ID = 1L;
    private static final List<Long> STAFF_IDS = List.of(101L, 102L, 103L);
    private static final Instant NOW = Instant.parse("2026-11-25T00:00:00Z");

    @Mock
    LoadInquiryPort loadInquiryPort;
    @Mock
    InquiryMessageAssembler messageAssembler;
    @Mock
    ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    @Mock
    InquiryRealtimeBroadcastPort broadcastPort;

    @InjectMocks
    InquiryRealtimeRelayService relayService;

    @Test
    @DisplayName("메시지는 작성자에게 먼저 보내고, 운영진 한 명에게 실패해도 나머지에게 보낸 뒤 예외를 다시 던진다")
    void 메시지_전달_중간_실패() {
        // given
        ChatMessageInfo message = new ChatMessageInfo(
            1L, ROOM_ID, 101L, MessageContentType.TEXT, "확인해 볼게요", List.of(), NOW
        );
        InquiryMessageInfo view = view(101L, "staff");
        Inquiry inquiry = 문의();
        given(loadInquiryPort.findByChatRoomId(ROOM_ID)).willReturn(Optional.of(inquiry));
        given(messageAssembler.assemble(inquiry, message)).willReturn(view);
        given(listMemberSystemRoleUseCase.listUmcProductMemberIds()).willReturn(STAFF_IDS);
        InquiryRealtimeEvent event = new InquiryRealtimeEvent.MessageCreated(INQUIRY_ID, view);
        brokerFailsFor(102L);

        // when & then
        assertThatThrownBy(() -> relayService.relayMessageCreated(message))
            .isInstanceOf(IllegalStateException.class)
            .satisfies(exception -> assertThat(exception.getSuppressed())
                .singleElement()
                .satisfies(suppressed -> assertThat(suppressed)
                    .hasMessage("broker down")));
        InOrder order = inOrder(broadcastPort);
        order.verify(broadcastPort).sendToMember(AUTHOR_ID, event);
        order.verify(broadcastPort).sendToMember(101L, event);
        order.verify(broadcastPort).sendToMember(102L, event);
        order.verify(broadcastPort).sendToMember(103L, event);
        order.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("상태 변경은 작성자에게 먼저 보내고, 운영진 한 명에게 실패해도 나머지에게 보낸 뒤 예외를 다시 던진다")
    void 상태_전달_중간_실패() {
        // given
        InquiryStatusChangedEvent event = new InquiryStatusChangedEvent(
            UUID.randomUUID(), NOW, INQUIRY_ID, AUTHOR_ID, InquiryStatus.CLOSED, NOW, NOW
        );
        InquiryRealtimeEvent statusEvent = new InquiryRealtimeEvent.StatusChanged(
            INQUIRY_ID, InquiryStatus.CLOSED, NOW, NOW, NOW
        );
        given(listMemberSystemRoleUseCase.listUmcProductMemberIds()).willReturn(STAFF_IDS);
        brokerFailsFor(102L);

        // when & then
        assertThatThrownBy(() -> relayService.relayStatusChanged(event))
            .isInstanceOf(IllegalStateException.class)
            .satisfies(exception -> assertThat(exception.getSuppressed())
                .singleElement()
                .satisfies(suppressed -> assertThat(suppressed).hasMessage("broker down")));
        InOrder order = inOrder(broadcastPort);
        order.verify(broadcastPort).sendToMember(AUTHOR_ID, statusEvent);
        order.verify(broadcastPort).sendToMember(101L, statusEvent);
        order.verify(broadcastPort).sendToMember(102L, statusEvent);
        order.verify(broadcastPort).sendToMember(103L, statusEvent);
        order.verifyNoMoreInteractions();
    }

    private void brokerFailsFor(Long failingMemberId) {
        willAnswer(invocation -> {
            if (failingMemberId.equals(invocation.getArgument(0))) {
                throw new IllegalStateException("broker down");
            }
            return null;
        }).given(broadcastPort).sendToMember(any(), any());
    }

    private static Inquiry 문의() {
        Inquiry inquiry = Inquiry.create(AUTHOR_ID, 1L, null, "앱 로그인이 안 돼요", ROOM_ID, UUID.randomUUID(), null, NOW);
        ReflectionTestUtils.setField(inquiry, "id", INQUIRY_ID);
        return inquiry;
    }

    private static InquiryMessageInfo view(Long senderMemberId, String senderName) {
        return new InquiryMessageInfo(1L, InquiryParticipant.STAFF, senderMemberId, senderName, "확인해 볼게요", List.of(), NOW);
    }
}
