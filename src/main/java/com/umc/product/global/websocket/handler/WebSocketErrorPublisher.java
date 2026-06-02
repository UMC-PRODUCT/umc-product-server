package com.umc.product.global.websocket.handler;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.code.BaseCode;
import java.security.Principal;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 세션 사용자에게 ApiResponse 형식의 에러 메시지를 전송한다.
 */
@Component
public class WebSocketErrorPublisher {

    private static final String USER_ERROR_DESTINATION = "/queue/errors";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketErrorPublisher(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 사용자가 구독 중인 /user/queue/errors destination으로 에러 응답을 보낸다.
     */
    public void sendErrorToUser(Principal user, BaseCode errorCode) {
        ApiResponse<Object> response = ApiResponse.onFailure(
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );

        messagingTemplate.convertAndSendToUser(user.getName(), USER_ERROR_DESTINATION, response);
    }
}
