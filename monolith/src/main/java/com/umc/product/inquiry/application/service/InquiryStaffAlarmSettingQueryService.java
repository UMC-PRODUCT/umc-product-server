package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStaffAlarmSettingQueryService implements GetInquiryStaffAlarmSettingUseCase {

    private final LoadInquiryStaffAlarmSettingPort loadSettingPort;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryStaffAlarmSettingInfo get(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        return loadSettingPort.findByMemberId(staffMemberId)
            .map(InquiryStaffAlarmSettingInfo::from)
            .orElseGet(InquiryStaffAlarmSettingInfo::defaultSetting);
    }
}
