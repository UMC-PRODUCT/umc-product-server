package com.umc.product.inquiry.application.port.out;

import java.util.List;

import com.umc.product.inquiry.domain.InquiryHistory;

public interface LoadInquiryHistoryPort {

    /** 오래된 것부터 돌려준다. */
    List<InquiryHistory> listByInquiryId(Long inquiryId);
}
