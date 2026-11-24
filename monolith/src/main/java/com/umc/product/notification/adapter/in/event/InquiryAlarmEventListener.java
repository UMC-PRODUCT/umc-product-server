package com.umc.product.notification.adapter.in.event;

import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.event.InquiryStaffRepliedEvent;
import com.umc.product.inquiry.domain.InquiryLinks;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 운영진 답변이 커밋된 뒤 outbox 를 거쳐 들어오며, 문의 작성자에게만 푸시를 보낸다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryAlarmEventListener {

    static final String TYPE_INQUIRY_REPLY = "INQUIRY_REPLY";
    private static final String TITLE = "문의 답변이 도착했어요";
    private static final String BODY = "UMC PRODUCT 팀이 문의에 답변했어요.";

    private final RequestFcmNotificationUseCase requestFcmNotificationUseCase;

    @EventListener
    public void handle(InquiryStaffRepliedEvent event) {
        requestFcmNotificationUseCase.request(RequestFcmNotificationCommand.builder()
            .requesterMemberId(event.staffMemberId())
            .memberIds(List.of(event.authorMemberId()))
            .title(TITLE)
            .body(BODY)
            .data(Map.of(
                "type", TYPE_INQUIRY_REPLY,
                "inquiryId", String.valueOf(event.inquiryId())
            ))
            .deepLink(InquiryLinks.appUrl(event.inquiryId()))
            .build());

        log.info("문의 답변 알림 요청: inquiryId={}, messageId={}", event.inquiryId(), event.messageId());
    }
}
