package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

public record InquiryStaffAlarmSettingInfo(boolean enabled) {

    public static InquiryStaffAlarmSettingInfo from(InquiryStaffAlarmSetting setting) {
        return new InquiryStaffAlarmSettingInfo(setting.isEnabled());
    }

    public static InquiryStaffAlarmSettingInfo defaultSetting() {
        return new InquiryStaffAlarmSettingInfo(InquiryStaffAlarmSetting.DEFAULT_ENABLED);
    }
}
