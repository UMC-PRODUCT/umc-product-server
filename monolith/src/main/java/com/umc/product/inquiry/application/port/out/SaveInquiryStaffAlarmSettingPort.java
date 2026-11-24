package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

public interface SaveInquiryStaffAlarmSettingPort {

    InquiryStaffAlarmSetting save(InquiryStaffAlarmSetting setting);
}
