package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;

public interface GetInquiryDetailUseCase {

    InquiryDetailInfo getDetail(InquiryDetailQuery query);
}
