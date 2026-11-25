package com.umc.product.inquiry.adapter.in.websocket;

import org.springframework.stereotype.Component;

import com.umc.product.global.websocket.application.port.in.StompSubscriptionAuthorizer;

/**
 * 사람별 큐는 서버가 받는 사람을 정해 보내므로 로그인한 회원이면 구독할 수 있고, 자기에게 보낸 이벤트만 받는다.
 */
@Component
public class InquiryStompSubscriptionAuthorizer implements StompSubscriptionAuthorizer {

    static final String USER_EVENT_DESTINATION = "/user/queue/inquiries/events";
    static final String RANKING_DESTINATION = "/topic/inquiries/ranking";

    @Override
    public boolean supports(String destination) {
        return USER_EVENT_DESTINATION.equals(destination) || RANKING_DESTINATION.equals(destination);
    }

    @Override
    public boolean isAuthorized(Long memberId, String destination) {
        return memberId != null && memberId > 0 && supports(destination);
    }
}
