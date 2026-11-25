package com.umc.product.inquiry.adapter.out.realtime;

import org.springframework.stereotype.Component;

import com.umc.product.global.websocket.application.port.out.BroadcastPort;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;
import com.umc.product.inquiry.application.port.out.InquiryRealtimeBroadcastPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryRealtimeBroadcastAdapter implements InquiryRealtimeBroadcastPort {

    private static final String USER_DESTINATION = "/queue/inquiries/events";

    private final BroadcastPort broadcastPort;

    @Override
    public void sendToMember(Long memberId, InquiryRealtimeEvent event) {
        broadcastPort.broadcastToUser(memberId.toString(), USER_DESTINATION, event);
    }
}
