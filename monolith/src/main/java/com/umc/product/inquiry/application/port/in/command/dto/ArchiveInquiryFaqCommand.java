package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * @param expectedVersion 요청자가 본 대표 질문의 버전. 그사이 다른 운영진이 고쳤으면 거절한다.
 */
public record ArchiveInquiryFaqCommand(
    Long actorMemberId,
    Long faqId,
    long expectedVersion
) {
}
