package com.umc.product.inquiry.application.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;
import com.umc.product.inquiry.application.port.in.realtime.RelayInquiryRealtimeEventUseCase;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 받는 사람은 문의 작성자와 {@code UMC_PRODUCT} 운영진 전원이다. 알림 설정과 상관없고, {@code SUPER_ADMIN}만 가진 회원은 받지 않는다.
 * 작성자에게 먼저 보내고, 한 명에게 실패해도 나머지에게 끝까지 보낸 뒤 실패를 한 번에 던져 outbox가 다시 시도하게 한다.
 */
@Service
@RequiredArgsConstructor
public class InquiryRealtimeRelayService implements RelayInquiryRealtimeEventUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final InquiryMessageAssembler messageAssembler;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final InquiryRealtimeBroadcastPort broadcastPort;

    @Override
    public void relayMessageCreated(ChatMessageInfo message) {
        loadInquiryPort.findByChatRoomId(message.roomId()).ifPresent(inquiry -> {
            InquiryMessageAssembler.Views views = messageAssembler.assembleViews(inquiry, message);
            List<Long> staffIds = listMemberSystemRoleUseCase.listUmcProductMemberIds();
            InquiryRealtimeEvent staffEvent = new InquiryRealtimeEvent.MessageCreated(inquiry.getId(), views.staff());

            Long authorId = inquiry.getAuthorMemberId();
            InquiryRealtimeEvent authorEvent = staffIds.contains(authorId)
                ? staffEvent
                : new InquiryRealtimeEvent.MessageCreated(
                    inquiry.getId(),
                    views.forViewer(messageAssembler.seesStaffIdentity(authorId))
                );
            deliver(authorFirst(authorId, authorEvent, staffIds, staffEvent));
        });
    }

    @Override
    public void relayStatusChanged(InquiryStatusChangedEvent event) {
        InquiryRealtimeEvent statusEvent = new InquiryRealtimeEvent.StatusChanged(
            event.inquiryId(),
            event.status(),
            event.closedAt(),
            event.lastMessageAt(),
            event.occurredAt()
        );
        deliver(authorFirst(
            event.authorMemberId(), statusEvent, listMemberSystemRoleUseCase.listUmcProductMemberIds(), statusEvent
        ));
    }

    private static Map<Long, InquiryRealtimeEvent> authorFirst(
        Long authorId,
        InquiryRealtimeEvent authorEvent,
        List<Long> staffIds,
        InquiryRealtimeEvent staffEvent
    ) {
        Map<Long, InquiryRealtimeEvent> deliveries = new LinkedHashMap<>();
        deliveries.put(authorId, authorEvent);
        staffIds.forEach(staffId -> deliveries.putIfAbsent(staffId, staffEvent));
        return deliveries;
    }

    private void deliver(Map<Long, InquiryRealtimeEvent> deliveries) {
        List<RuntimeException> failures = new ArrayList<>();
        deliveries.forEach((memberId, event) -> {
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
