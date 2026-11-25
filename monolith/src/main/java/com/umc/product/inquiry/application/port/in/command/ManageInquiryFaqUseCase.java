package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.ArchiveInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SaveInquiryFaqDraftCommand;

/**
 * 초안 저장·발행·보관은 요청자가 본 버전과 지금 버전이 다르면 {@code INQUIRY_FAQ_CHANGED}로 거절한다. 덮어쓰기는 없다.
 */
public interface ManageInquiryFaqUseCase {

    /** 초안을 만들고 ID를 돌려준다. */
    Long create(CreateInquiryFaqCommand command);

    /** 수정본만 바꾼다. 발행 중이면 기존 발행본이 그대로 공개된다. */
    void saveDraft(SaveInquiryFaqDraftCommand command);

    /** 수정본을 발행본으로 만든다. 보관된 대표 질문도 다시 발행할 수 있다. */
    void publish(PublishInquiryFaqCommand command);

    /** 공개·검색·추천·순위에서 바로 빠진다. 연결된 문의는 그대로다. */
    void archive(ArchiveInquiryFaqCommand command);
}
