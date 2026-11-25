package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.InquiryFaqContentCommand;
import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;

public interface ManageInquiryFaqUseCase {

    /** 초안을 만들고 ID를 돌려준다. */
    Long create(Long actorMemberId, InquiryFaqContentCommand content);

    /** 수정본만 바꾼다. 발행 중이면 기존 발행본이 그대로 공개된다. */
    void saveDraft(Long actorMemberId, Long faqId, InquiryFaqContentCommand content);

    /** 수정본을 발행본으로 만든다. 보관된 대표 질문도 다시 발행할 수 있다. */
    void publish(PublishInquiryFaqCommand command);

    /** 공개·검색·추천·순위에서 바로 빠진다. 연결된 문의는 그대로다. */
    void archive(Long actorMemberId, Long faqId);
}
