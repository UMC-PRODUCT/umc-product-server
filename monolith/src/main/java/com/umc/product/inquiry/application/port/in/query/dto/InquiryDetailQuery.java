package com.umc.product.inquiry.application.port.in.query.dto;

public record InquiryDetailQuery(
    Long inquiryId,
    Long requesterMemberId
) {
}
