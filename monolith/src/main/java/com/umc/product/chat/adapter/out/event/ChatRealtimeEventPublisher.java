package com.umc.product.chat.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.umc.product.chat.application.event.ChatRealtimeAfterCommitEvent;
import com.umc.product.chat.application.port.out.PublishChatRealtimePort;
import com.umc.product.chat.domain.event.ChatRealtimeEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ChatRealtimeEventPublisher implements PublishChatRealtimePort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishAfterCommit(ChatRealtimeEvent event) {
        eventPublisher.publishEvent(new ChatRealtimeAfterCommitEvent(event));
    }
}
