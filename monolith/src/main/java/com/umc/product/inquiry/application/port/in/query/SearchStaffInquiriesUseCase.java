package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffPageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.StaffInquiriesQuery;

public interface SearchStaffInquiriesUseCase {

    InquiryStaffPageInfo search(StaffInquiriesQuery query);
}
