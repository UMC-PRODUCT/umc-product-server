package com.umc.product.chat.adapter.in.event;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import com.umc.product.chat.adapter.in.web.dto.response.ChatMessageResponse;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.global.websocket.application.port.out.BroadcastPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 생성 이벤트를 수신하여 구독자에게 실시간 broadcast 한다.
 * <p>
 * 트랜잭션 커밋 직후(AFTER_COMMIT) 동기로 실행한다. broadcast 실패 시 예외를 삼키지 않고 다시 던져,
 * outbox relay 가 해당 이벤트를 markPublished 하지 못하고 폴러가 재처리(재broadcast)하도록 한다.
 * (at-least-once. 클라이언트는 messageId 로 중복을 제거한다.)
 * <p>
 * destination 경로 조립은 이 리스너의 책임이며, {@link BroadcastPort} 는 전달받은 destination 으로 그대로
 * 전송하기만 한다(브로커 교체 대비 디커플링). 특정 STOMP 구현({@code StompBroadcastAdapter})이 아니라
 * 포트 인터페이스에만 의존한다.
 * <p>
 * TODO: 처리량 부하가 확인되면 {@code @Async} 비동기 처리를 검토한다. 현재는 폴러 재처리 흐름을 단순하게 유지하기
 *  위해 동기로 둔다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBroadcastListener {

    private static final String CHAT_ROOM_DESTINATION_PREFIX = "/topic/chat/rooms/";
    private static final String CHAT_ROOM_DESTINATION_SUFFIX = "/messages";

    private final BroadcastPort broadcastPort;

    @EventListener
    public void handle(ChatMessageCreatedEvent event) {
        String destination = CHAT_ROOM_DESTINATION_PREFIX + event.roomId() + CHAT_ROOM_DESTINATION_SUFFIX;
        ChatMessageResponse payload = ChatMessageResponse.from(event);

        try {
            broadcastPort.broadcast(destination, payload);
        } catch (Exception e) {
            log.error("채팅 메시지 broadcast 실패: messageId={}, roomId={}, destination={}",
                event.messageId(), event.roomId(), destination, e);
            throw e;
        }
    }
}
