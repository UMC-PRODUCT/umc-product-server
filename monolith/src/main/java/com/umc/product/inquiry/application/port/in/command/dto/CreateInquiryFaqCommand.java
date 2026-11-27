package com.umc.product.inquiry.application.port.in.command.dto;

public record CreateInquiryFaqCommand(
    Long actorMemberId,
    InquiryFaqContentCommand content
) {
}
