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

    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

    private final CheckChatRoomAccessUseCase checkChatRoomAccessUseCase;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !isAuthorizationTarget(accessor.getCommand())) {
            return message;
        }

        Optional<Long> chatRoomId = extractChatRoomId(accessor.getDestination());
        if (chatRoomId.isEmpty()) {
            return message;
        }

        Long memberId = extractMemberId(accessor);
        if (memberId == null || !checkChatRoomAccessUseCase.hasChatRoomAccess(memberId, chatRoomId.get())) {
            throw new MessageDeliveryException("채팅방 접근 권한이 없습니다.");
        }

        return message;
    }

    private boolean isAuthorizationTarget(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command);
    }

    private Optional<Long> extractChatRoomId(String destination) {
        if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
            return Optional.empty();
        }

        String rawChatRoomId = destination.substring(CHAT_TOPIC_PREFIX.length());
        if (rawChatRoomId.isBlank() || rawChatRoomId.contains("/")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(rawChatRoomId));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private Long extractMemberId(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user instanceof UsernamePasswordAuthenticationToken auth
            && auth.getPrincipal() instanceof MemberPrincipal principal) {
            return principal.getMemberId();
        }
        return null;
    }
}
