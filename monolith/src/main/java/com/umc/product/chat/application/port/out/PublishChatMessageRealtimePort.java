package com.umc.product.chat.application.port.out;

import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;

public interface PublishChatMessageRealtimePort {

    void publishAfterCommit(ChatMessageCreatedEvent event);
}
