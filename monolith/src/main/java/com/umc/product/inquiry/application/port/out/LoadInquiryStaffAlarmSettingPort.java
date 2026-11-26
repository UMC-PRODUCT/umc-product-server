package com.umc.product.inquiry.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

public interface LoadInquiryStaffAlarmSettingPort {

    Optional<InquiryStaffAlarmSetting> findByMemberId(Long memberId);

    List<Long> listEnabledMemberIds();
}
