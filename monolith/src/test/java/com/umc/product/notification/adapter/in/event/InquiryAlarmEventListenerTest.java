package com.umc.product.notification.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.inquiry.application.event.InquiryAuthorMessagedEvent;
import com.umc.product.inquiry.application.event.InquiryCreatedEvent;
import com.umc.product.inquiry.application.event.InquiryStaffRepliedEvent;
import com.umc.product.inquiry.application.port.in.query.ListInquiryStaffAlarmRecipientsUseCase;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

@ExtendWith(MockitoExtension.class)
class InquiryAlarmEventListenerTest {

    @Mock
    RequestFcmNotificationUseCase requestFcmNotificationUseCase;
    @Mock
    ListInquiryStaffAlarmRecipientsUseCase listInquiryStaffAlarmRecipientsUseCase;

    @InjectMocks
    InquiryAlarmEventListener listener;

    @Test
    @DisplayName("운영진 답변이 오면 문의 작성자에게만 문의 ID를 담은 푸시를 요청한다")
    void 운영진_답변_알림() {
        // given
        InquiryStaffRepliedEvent event = InquiryStaffRepliedEvent.of(7L, 100L, 1L, 2L);

        // when
        listener.handle(event);

        // then
        RequestFcmNotificationCommand command = 요청();
        assertThat(command.memberIds()).isEqualTo(List.of(1L));
        assertThat(command.data()).isEqualTo(Map.of("type", "INQUIRY_REPLY", "inquiryId", "7"));
        assertThat(command.deepLink()).isEqualTo("umc://inquiry/7");
        assertThat(command.title()).isNotBlank();
        assertThat(command.body()).isNotBlank();
    }

    @Test
    @DisplayName("새 문의가 오면 새 문의 알림 대상 운영진에게 문의 제목을 본문으로 푸시를 요청한다")
    void 새_문의_알림() {
        // given
        given(listInquiryStaffAlarmRecipientsUseCase.listNewInquiryRecipientMemberIds(1L)).willReturn(List.of(3L, 4L));

        // when
        listener.handle(InquiryCreatedEvent.of(7L, 1L, "앱 로그인이 안 돼요"));

        // then
        RequestFcmNotificationCommand command = 요청();
        assertThat(command.memberIds()).isEqualTo(List.of(3L, 4L));
        assertThat(command.data()).isEqualTo(Map.of("type", "INQUIRY_NEW", "inquiryId", "7"));
        assertThat(command.deepLink()).isEqualTo("umc://inquiry/7");
        assertThat(command.title()).isEqualTo("새 문의가 왔어요");
        assertThat(command.body()).isEqualTo("앱 로그인이 안 돼요");
    }

    @Test
    @DisplayName("작성자 메시지가 오면 그 문의의 작성자 메시지 알림 대상 운영진에게 새 메시지 푸시를 요청한다")
    void 작성자_메시지_알림() {
        // given
        given(listInquiryStaffAlarmRecipientsUseCase.listAuthorMessageRecipientMemberIds(7L)).willReturn(List.of(3L));

        // when
        listener.handle(InquiryAuthorMessagedEvent.of(7L, 100L, 1L, "앱 로그인이 안 돼요"));

        // then
        RequestFcmNotificationCommand command = 요청();
        assertThat(command.memberIds()).isEqualTo(List.of(3L));
        assertThat(command.data()).isEqualTo(Map.of("type", "INQUIRY_AUTHOR_MESSAGE", "inquiryId", "7"));
        assertThat(command.title()).isEqualTo("문의에 새 메시지가 왔어요");
    }

    @Test
    @DisplayName("알릴 운영진이 없으면 푸시를 요청하지 않는다")
    void 대상_없음() {
        // given
        given(listInquiryStaffAlarmRecipientsUseCase.listNewInquiryRecipientMemberIds(1L)).willReturn(List.of());

        // when
        listener.handle(InquiryCreatedEvent.of(7L, 1L, "앱 로그인이 안 돼요"));

        // then
        verifyNoInteractions(requestFcmNotificationUseCase);
    }

    private RequestFcmNotificationCommand 요청() {
        ArgumentCaptor<RequestFcmNotificationCommand> captor = ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        verify(requestFcmNotificationUseCase).request(captor.capture());
        return captor.getValue();
    }
}
