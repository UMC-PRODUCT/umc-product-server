package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.SaveInquiryLabelCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;

public interface ManageInquiryLabelUseCase {

    InquiryLabelInfo create(SaveInquiryLabelCommand command);

    InquiryLabelInfo rename(SaveInquiryLabelCommand command);

    /** 붙어 있던 문의에서도 함께 빠진다. */
    void delete(Long actorMemberId, Long labelId);
}
