package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryAssigneeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record ChangeInquiryAssigneeRequest(
    @Schema(description = "새 담당자 회원 ID. null이면 담당자를 비운다. 내가 맡을 때는 내 회원 ID", nullable = true)
    @Positive Long assigneeMemberId,
    @Schema(description = "화면에서 본 지금 담당자 회원 ID. 미배정이었으면 null. 지금 담당자와 다르면 409", nullable = true)
    @Positive Long expectedAssigneeMemberId
) {

    public ChangeInquiryAssigneeCommand toCommand(Long inquiryId, Long actorMemberId) {
        return new ChangeInquiryAssigneeCommand(inquiryId, actorMemberId, assigneeMemberId, expectedAssigneeMemberId);
    }
}
