package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.UpdateInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryStaffAlarmSettingCommandService implements UpdateInquiryStaffAlarmSettingUseCase {

    private final LoadInquiryStaffAlarmSettingPort loadSettingPort;
    private final SaveInquiryStaffAlarmSettingPort saveSettingPort;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryStaffAlarmSettingInfo update(UpdateInquiryStaffAlarmSettingCommand command) {
        accessPolicy.requireStaff(command.staffMemberId());
        InquiryStaffAlarmSetting setting = loadSettingPort.findByMemberId(command.staffMemberId())
            .orElseGet(() -> InquiryStaffAlarmSetting.create(command.staffMemberId(), command.enabled()));
        setting.changeEnabled(command.enabled());
        return InquiryStaffAlarmSettingInfo.from(saveSettingPort.save(setting));
    }
}
