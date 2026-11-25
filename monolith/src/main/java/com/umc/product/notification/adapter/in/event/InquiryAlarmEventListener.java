package com.umc.product.notification.adapter.in.event;

import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.event.InquiryAuthorMessagedEvent;
import com.umc.product.inquiry.application.event.InquiryCreatedEvent;
import com.umc.product.inquiry.application.event.InquiryStaffRepliedEvent;
import com.umc.product.inquiry.application.port.in.query.ListInquiryStaffAlarmRecipientsUseCase;
import com.umc.product.inquiry.domain.InquiryLinks;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryAlarmEventListener {

    static final String TYPE_INQUIRY_REPLY = "INQUIRY_REPLY";
    static final String TYPE_INQUIRY_NEW = "INQUIRY_NEW";
    static final String TYPE_INQUIRY_AUTHOR_MESSAGE = "INQUIRY_AUTHOR_MESSAGE";
    private static final String REPLY_TITLE = "문의 답변이 도착했어요";
    private static final String REPLY_BODY = "UMC PRODUCT 팀이 문의에 답변했어요.";
    private static final String NEW_INQUIRY_TITLE = "새 문의가 왔어요";
    private static final String AUTHOR_MESSAGE_TITLE = "문의에 새 메시지가 왔어요";

    private final RequestFcmNotificationUseCase requestFcmNotificationUseCase;
    private final ListInquiryStaffAlarmRecipientsUseCase listInquiryStaffAlarmRecipientsUseCase;

    @EventListener
    public void handle(InquiryStaffRepliedEvent event) {
        request(event.staffMemberId(), List.of(event.authorMemberId()), REPLY_TITLE, REPLY_BODY,
            TYPE_INQUIRY_REPLY, event.inquiryId());

        log.info("문의 답변 알림 요청: inquiryId={}, messageId={}", event.inquiryId(), event.messageId());
    }

    @EventListener
    public void handle(InquiryCreatedEvent event) {
        notifyStaff(
            listInquiryStaffAlarmRecipientsUseCase.listNewInquiryRecipientMemberIds(event.authorMemberId()),
            event.authorMemberId(), NEW_INQUIRY_TITLE, event.title(), TYPE_INQUIRY_NEW, event.inquiryId()
        );
    }

    @EventListener
    public void handle(InquiryAuthorMessagedEvent event) {
        notifyStaff(
            listInquiryStaffAlarmRecipientsUseCase.listAuthorMessageRecipientMemberIds(event.inquiryId()),
            event.authorMemberId(), AUTHOR_MESSAGE_TITLE, event.title(), TYPE_INQUIRY_AUTHOR_MESSAGE, event.inquiryId()
        );
    }

    private void notifyStaff(List<Long> recipients, Long senderMemberId, String title, String body, String type,
        Long inquiryId) {
        if (recipients.isEmpty()) {
            return;
        }
        request(senderMemberId, recipients, title, body, type, inquiryId);

        log.info("문의 운영진 알림 요청: type={}, inquiryId={}, recipients={}", type, inquiryId, recipients.size());
    }

    private void request(Long requesterMemberId, List<Long> memberIds, String title, String body, String type,
        Long inquiryId) {
        requestFcmNotificationUseCase.request(RequestFcmNotificationCommand.builder()
            .requesterMemberId(requesterMemberId)
            .memberIds(memberIds)
            .title(title)
            .body(body)
            .data(Map.of(
                "type", type,
                "inquiryId", String.valueOf(inquiryId)
            ))
            .deepLink(InquiryLinks.appUrl(inquiryId))
            .build());
    }
}
