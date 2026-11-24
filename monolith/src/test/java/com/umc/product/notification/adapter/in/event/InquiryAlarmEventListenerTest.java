package com.umc.product.notification.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.inquiry.application.event.InquiryStaffRepliedEvent;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

@ExtendWith(MockitoExtension.class)
class InquiryAlarmEventListenerTest {

    @Mock
    RequestFcmNotificationUseCase requestFcmNotificationUseCase;

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
        ArgumentCaptor<RequestFcmNotificationCommand> captor = ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        verify(requestFcmNotificationUseCase).request(captor.capture());
        RequestFcmNotificationCommand command = captor.getValue();
        assertThat(command.memberIds()).isEqualTo(List.of(1L));
        assertThat(command.data()).isEqualTo(Map.of("type", "INQUIRY_REPLY", "inquiryId", "7"));
        assertThat(command.deepLink()).isEqualTo("umc://inquiry/7");
        assertThat(command.title()).isNotBlank();
        assertThat(command.body()).isNotBlank();
    }
}
