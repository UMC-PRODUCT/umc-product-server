package com.umc.product.chat.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.umc.product.chat.application.event.ChatMessageCreatedAfterCommitEvent;
import com.umc.product.chat.application.port.out.PublishChatMessageRealtimePort;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ChatMessageRealtimeEventPublisher implements PublishChatMessageRealtimePort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishAfterCommit(ChatMessageCreatedEvent event) {
        eventPublisher.publishEvent(new ChatMessageCreatedAfterCommitEvent(event));
    }
}
