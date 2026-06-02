package com.umc.product.global.websocket.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.umc.product.chat.application.port.in.query.CheckChatRoomAccessUseCase;
import com.umc.product.global.security.MemberPrincipal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@DisplayName("StompAuthChannelInterceptor")
@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {

    @Mock
    private CheckChatRoomAccessUseCase checkChatRoomAccessUseCase;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private StompAuthChannelInterceptor sut;

    @Test
    @DisplayName("채팅방 접근 권한이 있는 멤버의 SUBSCRIBE 프레임은 통과된다")
    void subscribe_with_access_passes() {
        when(checkChatRoomAccessUseCase.hasChatRoomAccess(1L, 10L)).thenReturn(true);
        Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, "/topic/chat/10", 1L);

        Message<?> result = sut.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verify(checkChatRoomAccessUseCase).hasChatRoomAccess(1L, 10L);
    }

    @Test
    @DisplayName("채팅방 접근 권한이 없는 멤버의 SEND 프레임은 MessageDeliveryException이 발생한다")
    void send_without_access_throws() {
        when(checkChatRoomAccessUseCase.hasChatRoomAccess(1L, 10L)).thenReturn(false);
        Message<byte[]> message = stompMessage(StompCommand.SEND, "/topic/chat/10", 1L);

        assertThatThrownBy(() -> sut.preSend(message, channel))
            .isInstanceOf(MessageDeliveryException.class)
            .hasMessageContaining("채팅방 접근 권한");
    }

    @Test
    @DisplayName("Principal이 없는 채팅방 프레임은 MessageDeliveryException이 발생한다")
    void chat_frame_without_principal_throws() {
        Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, "/topic/chat/10", null);

        assertThatThrownBy(() -> sut.preSend(message, channel))
            .isInstanceOf(MessageDeliveryException.class)
            .hasMessageContaining("채팅방 접근 권한");
    }

    @Test
    @DisplayName("SEND와 SUBSCRIBE가 아닌 프레임은 인가 처리 없이 통과된다")
    void non_target_command_passes() {
        Message<byte[]> message = stompMessage(StompCommand.CONNECT, "/topic/chat/10", 1L);

        assertThat(sut.preSend(message, channel)).isSameAs(message);
        verifyNoInteractions(checkChatRoomAccessUseCase);
    }

    @Test
    @DisplayName("채팅방 destination으로 파싱할 수 없는 프레임은 인가 처리 없이 통과된다")
    void invalid_chat_destination_passes() {
        Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, "/topic/chat/not-number", 1L);

        assertThat(sut.preSend(message, channel)).isSameAs(message);
        verifyNoInteractions(checkChatRoomAccessUseCase);
    }

    private Message<byte[]> stompMessage(StompCommand command, String destination, Long memberId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        if (memberId != null) {
            MemberPrincipal principal = MemberPrincipal.builder().memberId(memberId).build();
            accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
