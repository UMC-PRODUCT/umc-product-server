package com.umc.product.inquiry.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;
import com.umc.product.inquiry.application.policy.InquiryStaffRoster;
import com.umc.product.inquiry.application.port.in.realtime.RelayInquiryRealtimeEventUseCase;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryRealtimeRelayService implements RelayInquiryRealtimeEventUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final InquiryMessageAssembler messageAssembler;
    private final InquiryStaffRoster staffRoster;
    private final InquiryRealtimeBroadcastPort broadcastPort;

    @Override
    public void relayMessageCreated(ChatMessageInfo message) {
        loadInquiryPort.findByChatRoomId(message.roomId()).ifPresent(inquiry -> deliver(
            inquiry.getAuthorMemberId(),
            new InquiryRealtimeEvent.MessageCreated(inquiry.getId(), messageAssembler.assemble(inquiry, message))
        ));
    }

    @Override
    public void relayStatusChanged(InquiryStatusChangedEvent event) {
        deliver(event.authorMemberId(), new InquiryRealtimeEvent.StatusChanged(
            event.inquiryId(),
            event.status(),
            event.closedAt(),
            event.lastMessageAt(),
            event.occurredAt()
        ));
    }

    private void deliver(Long authorId, InquiryRealtimeEvent event) {
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(authorId);
        recipients.addAll(staffRoster.memberIds());
        List<RuntimeException> failures = new ArrayList<>();
        recipients.forEach(memberId -> {
            try {
                broadcastPort.sendToMember(memberId, event);
            } catch (RuntimeException exception) {
                failures.add(exception);
            }
        });
        if (!failures.isEmpty()) {
            IllegalStateException aggregate = new IllegalStateException(
                "Inquiry realtime fan-out failed: failures=" + failures.size()
            );
            failures.forEach(aggregate::addSuppressed);
            throw aggregate;
        }
    }
}
