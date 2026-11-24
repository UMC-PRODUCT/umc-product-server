package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;

public record InquiryStaffAlarmSettingResponse(boolean enabled) {

    public static InquiryStaffAlarmSettingResponse from(InquiryStaffAlarmSettingInfo info) {
        return new InquiryStaffAlarmSettingResponse(info.enabled());
    }
}
