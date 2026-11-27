package com.umc.product.inquiry.application.port.in.query.dto;

/**
 * @param cursor 이전 페이지 마지막 메시지 ID. 첫 페이지(최신 메시지부터)는 null.
 */
public record InquiryMessagesQuery(
    Long inquiryId,
    Long requesterMemberId,
    Long cursor,
    int size
) {
}
