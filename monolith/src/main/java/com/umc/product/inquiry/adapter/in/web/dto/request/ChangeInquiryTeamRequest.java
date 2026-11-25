package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryTeamCommand;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChangeInquiryTeamRequest(
    @Schema(description = "받는 팀. null이면 어느 팀인지 모르겠어요", nullable = true)
    InquiryTeam targetTeam
) {

    public ChangeInquiryTeamCommand toCommand(Long inquiryId, Long actorMemberId) {
        return new ChangeInquiryTeamCommand(inquiryId, actorMemberId, targetTeam);
    }
}
