package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * @param faqId null이면 연결을 끊는다.
 */
public record LinkInquiryFaqCommand(
    Long inquiryId,
    Long actorMemberId,
    Long faqId
) {
}
