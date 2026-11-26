package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryFaqRankingSignal;
import com.umc.product.inquiry.application.port.in.realtime.dto.InquiryRealtimeEvent;

public interface InquiryRealtimeBroadcastPort {

    void sendToMember(Long memberId, InquiryRealtimeEvent event);

    /** 로그인한 누구나 구독하는 공용 주소로 보낸다. */
    void broadcastRankingSignal(InquiryFaqRankingSignal signal);
}
