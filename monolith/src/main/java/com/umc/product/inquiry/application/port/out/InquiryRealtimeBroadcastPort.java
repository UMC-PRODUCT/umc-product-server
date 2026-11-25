package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;

public interface InquiryRealtimeBroadcastPort {

    void sendToMember(Long memberId, InquiryRealtimeEvent event);
}
