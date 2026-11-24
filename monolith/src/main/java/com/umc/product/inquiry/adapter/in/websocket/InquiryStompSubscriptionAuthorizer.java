package com.umc.product.inquiry.adapter.in.websocket;

import org.springframework.stereotype.Component;

import com.umc.product.global.websocket.application.port.in.StompSubscriptionAuthorizer;
import com.umc.product.inquiry.application.port.in.query.CheckInquiryAccessUseCase;
import com.umc.product.inquiry.application.port.in.realtime.InquiryRealtimeTopic;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryStompSubscriptionAuthorizer implements StompSubscriptionAuthorizer {

    private final CheckInquiryAccessUseCase checkInquiryAccessUseCase;

    @Override
    public boolean supports(String destination) {
        return InquiryRealtimeTopic.matches(destination);
    }

    @Override
    public boolean isAuthorized(Long memberId, String destination) {
        return InquiryRealtimeTopic.parseInquiryId(destination)
            .map(inquiryId -> checkInquiryAccessUseCase.canAccess(memberId, inquiryId))
            .orElse(false);
    }
}
