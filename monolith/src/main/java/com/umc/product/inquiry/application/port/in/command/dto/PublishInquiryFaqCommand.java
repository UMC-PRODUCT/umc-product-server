package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * 발행 전 검증 체크 세 개. 모두 true여야 발행하고, 체크 자체는 저장하지 않는다.
 *
 * @param expectedVersion 요청자가 본 대표 질문의 버전. 그사이 다른 운영진이 고쳤으면 거절한다.
 */
public record PublishInquiryFaqCommand(
    Long actorMemberId,
    Long faqId,
    long expectedVersion,
    boolean noPersonalInformation,
    boolean scopeConfirmed,
    boolean answerVerified
) {
    public boolean verified() {
        return noPersonalInformation && scopeConfirmed && answerVerified;
    }
}
