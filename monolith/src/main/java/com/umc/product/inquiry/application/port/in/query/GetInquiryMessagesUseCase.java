package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagePageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagesQuery;

public interface GetInquiryMessagesUseCase {

    InquiryMessagePageInfo getMessages(InquiryMessagesQuery query);
}
