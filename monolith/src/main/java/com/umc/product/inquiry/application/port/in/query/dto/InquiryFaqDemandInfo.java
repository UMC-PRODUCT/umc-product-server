package com.umc.product.inquiry.application.port.in.query.dto;

/**
 * 답변 준비가 필요한 초안 대표 질문.
 *
 * @param askedMemberCount 최근 24시간 동안 이 초안에 연결된 문의를 남긴 서로 다른 회원 수
 */
public record InquiryFaqDemandInfo(
    Long faqId,
    String question,
    long askedMemberCount
) {
}
