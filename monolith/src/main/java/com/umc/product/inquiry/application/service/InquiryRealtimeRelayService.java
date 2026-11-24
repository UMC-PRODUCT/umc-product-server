package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.global.websocket.application.port.out.BroadcastPort;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;
import com.umc.product.inquiry.application.port.in.realtime.InquiryRealtimeTopic;
import com.umc.product.inquiry.application.port.in.realtime.RelayInquiryRealtimeEventUseCase;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryRealtimeRelayService implements RelayInquiryRealtimeEventUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final InquiryMessageAssembler messageAssembler;
    private final BroadcastPort broadcastPort;

    @Override
    public void relayMessageCreated(ChatMessageInfo message) {
        loadInquiryPort.findByChatRoomId(message.roomId()).ifPresent(inquiry -> broadcastPort.broadcast(
            InquiryRealtimeTopic.of(inquiry.getId()),
            new InquiryRealtimeEvent.MessageCreated(inquiry.getId(), messageAssembler.assemble(inquiry, message))
        ));
    }

    @Override
    public void relayStatusChanged(InquiryStatusChangedEvent event) {
        broadcastPort.broadcast(
            InquiryRealtimeTopic.of(event.inquiryId()),
            new InquiryRealtimeEvent.StatusChanged(
                event.inquiryId(),
                event.status(),
                event.closedAt(),
                event.lastMessageAt(),
                event.occurredAt()
            )
        );
    }
}
