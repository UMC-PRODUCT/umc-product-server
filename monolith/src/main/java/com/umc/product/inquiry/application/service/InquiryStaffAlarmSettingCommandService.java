package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.UpdateInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;
import com.umc.product.inquiry.application.port.out.SaveInquiryStaffAlarmSettingPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryStaffAlarmSettingCommandService implements UpdateInquiryStaffAlarmSettingUseCase {

    private final SaveInquiryStaffAlarmSettingPort saveSettingPort;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryStaffAlarmSettingInfo update(UpdateInquiryStaffAlarmSettingCommand command) {
        accessPolicy.requireStaff(command.staffMemberId());
        saveSettingPort.upsert(command.staffMemberId(), command.enabled());
        return new InquiryStaffAlarmSettingInfo(command.enabled());
    }
}
