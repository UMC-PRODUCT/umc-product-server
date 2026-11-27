package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;

public interface UpdateInquiryStaffAlarmSettingUseCase {

    InquiryStaffAlarmSettingInfo update(UpdateInquiryStaffAlarmSettingCommand command);
}
