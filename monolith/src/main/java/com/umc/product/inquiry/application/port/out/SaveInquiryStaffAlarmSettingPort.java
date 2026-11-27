package com.umc.product.inquiry.application.port.out;

public interface SaveInquiryStaffAlarmSettingPort {

    void upsert(Long memberId, boolean enabled);
}
