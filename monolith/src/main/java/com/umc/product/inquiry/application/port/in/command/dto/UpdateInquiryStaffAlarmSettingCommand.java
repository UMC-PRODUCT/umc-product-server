package com.umc.product.inquiry.application.port.in.command.dto;

public record UpdateInquiryStaffAlarmSettingCommand(Long staffMemberId, boolean enabled) {
}
