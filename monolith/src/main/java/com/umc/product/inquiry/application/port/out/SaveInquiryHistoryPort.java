package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.domain.InquiryHistory;

public interface SaveInquiryHistoryPort {

    InquiryHistory save(InquiryHistory history);
}
