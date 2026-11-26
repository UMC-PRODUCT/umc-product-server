package com.umc.product.chat.application.port.out;

import com.umc.product.chat.domain.event.ChatRealtimeEvent;

public interface PublishChatRealtimePort {

    void publishAfterCommit(ChatRealtimeEvent event);
}
