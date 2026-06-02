package com.umc.product.global.websocket.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.global.response.ApiResponse;
import java.security.Principal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@DisplayName("WebSocketErrorPublisher")
class WebSocketErrorPublisherTest {

    private final SimpMessagingTemplate messagingTemplate =
        org.mockito.Mockito.mock(SimpMessagingTemplate.class);
    private final WebSocketErrorPublisher sut = new WebSocketErrorPublisher(messagingTemplate);

    @Test
    @DisplayName("사용자의 에러 큐로 ApiResponse 형식의 실패 응답을 전송한다")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void send_error_to_user() {
        Principal user = () -> "member-1";

        sut.sendErrorToUser(user, AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);

        ArgumentCaptor<ApiResponse<Object>> responseCaptor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(messagingTemplate).convertAndSendToUser(
            org.mockito.ArgumentMatchers.eq("member-1"),
            org.mockito.ArgumentMatchers.eq("/queue/errors"),
            responseCaptor.capture()
        );

        ApiResponse<Object> response = responseCaptor.getValue();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("AUTHORIZATION-0002");
        assertThat(response.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
        assertThat(response.getResult()).isNull();
    }
}
