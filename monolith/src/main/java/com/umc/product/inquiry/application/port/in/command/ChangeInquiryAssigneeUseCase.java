package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryAssigneeCommand;

public interface ChangeInquiryAssigneeUseCase {

    /**
     * 요청자가 본 담당자와 지금 담당자가 다르면 {@code INQUIRY_ASSIGNEE_CHANGED}로 거절한다. 덮어쓰기는 없다.
     */
    void changeAssignee(ChangeInquiryAssigneeCommand command);
}
