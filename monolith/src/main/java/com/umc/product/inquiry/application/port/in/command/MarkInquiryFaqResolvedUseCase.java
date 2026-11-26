package com.umc.product.inquiry.application.port.in.command;

public interface MarkInquiryFaqResolvedUseCase {

    /** 회원마다 대표 질문 하나에 한 번만 센다. 다시 눌러도 결과가 같다. */
    void markResolved(Long memberId, Long faqId);
}
