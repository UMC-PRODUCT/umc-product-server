package com.umc.product.chat.domain.event;

import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.OutboxDispatchMode;

public sealed interface ChatRealtimeEvent extends DomainEvent
    permits ChatMessageCreatedEvent, ChatMessageUpdatedEvent, ChatMessageDeletedEvent,
    ChatMessageReactionChangedEvent, ChatReadUpdatedEvent {

    @Override
    default OutboxDispatchMode outboxDispatchMode() {
        return OutboxDispatchMode.NON_TRANSACTIONAL;
    }
}
