package com.umc.product.global.websocket.interceptor;

import com.umc.product.chat.application.port.in.query.CheckChatRoomAccessUseCase;
import com.umc.product.global.security.MemberPrincipal;
import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    // 클라이언트가 채팅 메시지를 서버로 보낼 때 사용하는 주소
    private static final String CHAT_APP_PREFIX = "/app/chat/";
    // 클라이언트가 채팅방 메시지를 받기 위해 구독하는 주소
    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

    private final CheckChatRoomAccessUseCase checkChatRoomAccessUseCase;

    /**
     * 클라이언트에서 들어오는 STOMP 프레임 중 채팅방 접근 권한이 필요한 프레임을 검사한다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !isAuthorizationTarget(accessor.getCommand())) {
            return message;
        }

        Optional<Long> chatRoomId = extractChatRoomId(accessor.getCommand(), accessor.getDestination());
        if (chatRoomId.isEmpty()) {
            return message;
        }

        Long memberId = extractMemberId(accessor);
        if (memberId == null || !checkChatRoomAccessUseCase.hasChatRoomAccess(memberId, chatRoomId.get())) {
            throw new MessageDeliveryException("채팅방 접근 권한이 없습니다.");
        }

        return message;
    }

    /**
     * 채팅방 접근 권한 검사가 필요한 STOMP 명령인지 확인한다.
     */
    private boolean isAuthorizationTarget(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command);
    }

    /**
     * STOMP 명령과 destination에 맞는 채팅방 ID를 추출한다.
     */
    private Optional<Long> extractChatRoomId(StompCommand command, String destination) {
        if (StompCommand.SUBSCRIBE.equals(command)) {
            return extractChatRoomId(destination, CHAT_TOPIC_PREFIX);
        }
        if (StompCommand.SEND.equals(command)) {
            return extractChatRoomId(destination, CHAT_APP_PREFIX);
        }
        return Optional.empty();
    }

    /**
     * 지정된 prefix 뒤에 있는 채팅방 ID를 Long 타입으로 파싱한다.
     */
    private Optional<Long> extractChatRoomId(String destination, String prefix) {
        if (destination == null || !destination.startsWith(prefix)) {
            return Optional.empty();
        }

        String rawChatRoomId = destination.substring(prefix.length());
        if (rawChatRoomId.isBlank() || rawChatRoomId.contains("/")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(rawChatRoomId));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * STOMP 사용자 정보에서 인증된 회원 ID를 꺼낸다.
     */
    private Long extractMemberId(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user instanceof UsernamePasswordAuthenticationToken auth
            && auth.getPrincipal() instanceof MemberPrincipal principal) {
            return principal.getMemberId();
        }
        return null;
    }
}
