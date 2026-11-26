package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;

public interface GetInquiryStaffAlarmSettingUseCase {

    InquiryStaffAlarmSettingInfo get(Long staffMemberId);
}
