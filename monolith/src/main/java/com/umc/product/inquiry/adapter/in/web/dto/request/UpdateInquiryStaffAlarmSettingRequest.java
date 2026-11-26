package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateInquiryStaffAlarmSettingRequest(
    @Schema(description = "새 문의와 작성자 메시지 푸시를 받을지 여부")
    @NotNull Boolean enabled
) {

    public UpdateInquiryStaffAlarmSettingCommand toCommand(Long staffMemberId) {
        return new UpdateInquiryStaffAlarmSettingCommand(staffMemberId, enabled);
    }
}
