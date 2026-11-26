package com.umc.product.inquiry.application.port.in.command.dto;

public record CloseInquiryCommand(
    Long inquiryId,
    Long staffMemberId
) {
}
