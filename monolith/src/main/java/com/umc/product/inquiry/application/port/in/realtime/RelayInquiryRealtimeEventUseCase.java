package com.umc.product.inquiry.application.port.in.realtime;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.inquiry.application.event.InquiryStatusChangedEvent;

public interface RelayInquiryRealtimeEventUseCase {

    /**
     * 문의 방의 메시지가 아니면 아무것도 보내지 않는다.
     */
    void relayMessageCreated(ChatMessageInfo message);

    void relayStatusChanged(InquiryStatusChangedEvent event);
}
