package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * @param canSendMessage 요청자가 메시지를 보낼 수 있는지. 종료된 문의에 보내면 다시 진행 중이 된다.
 * @param canClose       요청자가 문의를 종료할 수 있는지. 운영진이고 아직 종료되지 않았을 때만 true.
 */
public record InquiryDetailInfo(
    Long inquiryId,
    String title,
    InquiryCategoryInfo category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    InquiryAuthorInfo author,
    Instant createdAt,
    Instant closedAt,
    Instant lastMessageAt,
    boolean canSendMessage,
    boolean canClose
) {
}
