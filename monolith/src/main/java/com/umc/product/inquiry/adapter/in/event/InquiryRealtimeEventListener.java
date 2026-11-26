package com.umc.product.inquiry.adapter.in.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;
import com.umc.product.inquiry.application.port.in.realtime.RelayInquiryRealtimeEventUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryRealtimeEventListener {

    private final RelayInquiryRealtimeEventUseCase relayUseCase;

    @EventListener
    public void onChatMessageCreated(ChatMessageCreatedEvent event) {
        relayUseCase.relayMessageCreated(new ChatMessageInfo(
            event.messageId(),
            event.roomId(),
            event.senderMemberId(),
            event.contentType(),
            event.content(),
            event.fileMetadataIds(),
            event.occurredAt()
        ));
    }

    @EventListener
    public void onStatusChanged(InquiryStatusChangedEvent event) {
        relayUseCase.relayStatusChanged(event);
    }
}
