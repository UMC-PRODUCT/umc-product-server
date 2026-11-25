package com.umc.product.inquiry.application.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
            staffIds.forEach(staffId -> broadcastPort.sendToMember(staffId, staffEvent));

            Long authorId = inquiry.getAuthorMemberId();
            if (!staffIds.contains(authorId)) {
                broadcastPort.sendToMember(authorId, new InquiryRealtimeEvent.MessageCreated(
                    inquiry.getId(),
                    views.forViewer(messageAssembler.seesStaffIdentity(authorId))
                ));
            }
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
        Set<Long> recipients = new LinkedHashSet<>(listMemberSystemRoleUseCase.listUmcProductMemberIds());
        recipients.add(event.authorMemberId());
        recipients.forEach(memberId -> broadcastPort.sendToMember(memberId, statusEvent));
    }
}
