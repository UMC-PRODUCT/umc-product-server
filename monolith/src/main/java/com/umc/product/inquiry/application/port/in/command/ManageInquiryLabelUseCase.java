package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;

public interface ManageInquiryLabelUseCase {

    InquiryLabelInfo create(Long actorMemberId, String name);

    InquiryLabelInfo rename(Long actorMemberId, Long labelId, String name);

    /** 붙어 있던 문의에서도 함께 빠진다. */
    void delete(Long actorMemberId, Long labelId);
}
