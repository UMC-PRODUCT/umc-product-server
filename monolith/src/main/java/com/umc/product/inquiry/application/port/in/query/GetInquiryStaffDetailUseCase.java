package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffDetailInfo;

public interface GetInquiryStaffDetailUseCase {

    InquiryStaffDetailInfo getStaffDetail(InquiryDetailQuery query);
}
