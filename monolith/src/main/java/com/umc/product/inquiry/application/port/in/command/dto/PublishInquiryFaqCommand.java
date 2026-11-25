package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * 발행 전 검증 체크 세 개. 모두 true여야 발행하고, 체크 자체는 저장하지 않는다.
 */
public record PublishInquiryFaqCommand(
    Long actorMemberId,
    Long faqId,
    boolean noPersonalInformation,
    boolean scopeConfirmed,
    boolean answerVerified
) {
    public boolean verified() {
        return noPersonalInformation && scopeConfirmed && answerVerified;
    }
}
