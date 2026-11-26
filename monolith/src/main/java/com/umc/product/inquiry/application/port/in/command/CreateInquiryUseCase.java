package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;

public interface CreateInquiryUseCase {

    /**
     * 문의를 등록하고 첫 메시지로 문의 내용을 남긴다. 같은 작성자가 같은 {@code clientRequestId}로 다시 요청하면
     * 새로 만들지 않고 기존 문의 ID를 돌려준다.
     */
    Long create(CreateInquiryCommand command);
}
