package com.umc.product.inquiry.adapter.in.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.inquiry.application.port.in.query.CheckInquiryAccessUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("문의 STOMP 구독 주소 인가")
class InquiryStompSubscriptionAuthorizerTest {

    @Mock
    CheckInquiryAccessUseCase checkInquiryAccessUseCase;

    @InjectMocks
    InquiryStompSubscriptionAuthorizer authorizer;

    @Test
    @DisplayName("문의 주소의 id로 접근 권한을 확인한다")
    void 문의_주소() {
        // given
        when(checkInquiryAccessUseCase.canAccess(7L, 12L)).thenReturn(true);
        when(checkInquiryAccessUseCase.canAccess(8L, 12L)).thenReturn(false);

        // when & then
        assertThat(authorizer.supports("/topic/inquiries/12")).isTrue();
        assertThat(authorizer.isAuthorized(7L, "/topic/inquiries/12")).isTrue();
        assertThat(authorizer.isAuthorized(8L, "/topic/inquiries/12")).isFalse();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "/topic/inquiry/12",
        "/topic/inquiries",
        "/topic/inquiries.12",
        "/topic/community/threads/12",
        "/user/queue/community/threads/events"
    })
    @DisplayName("문의 주소가 아니면 맡지 않는다")
    void 다른_주소(String destination) {
        assertThat(authorizer.supports(destination)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/topic/inquiries/",
        "/topic/inquiries/abc",
        "/topic/inquiries/0",
        "/topic/inquiries/012",
        "/topic/inquiries/-1",
        "/topic/inquiries/12/messages",
        "/topic/inquiries/12?x=1",
        "/topic/inquiries/99999999999999999999"
    })
    @DisplayName("문의 주소 아래의 잘못된 id는 권한 조회 없이 거절한다")
    void 잘못된_id(String destination) {
        // when & then
        assertThat(authorizer.supports(destination)).isTrue();
        assertThat(authorizer.isAuthorized(7L, destination)).isFalse();
        verify(checkInquiryAccessUseCase, never()).canAccess(any(), any());
    }
}
