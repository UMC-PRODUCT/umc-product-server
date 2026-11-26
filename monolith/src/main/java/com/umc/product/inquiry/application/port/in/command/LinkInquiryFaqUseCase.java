package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.LinkInquiryFaqCommand;

public interface LinkInquiryFaqUseCase {

    /** 문의를 대표 질문 하나에 연결하거나 끊는다. 초안 대표 질문에도 연결할 수 있다. */
    void linkFaq(LinkInquiryFaqCommand command);

    /** 문의 제목과 카테고리로 새 초안을 만들어 연결하고 그 ID를 돌려준다. */
    Long linkNewDraftFaq(Long inquiryId, Long actorMemberId);
}
