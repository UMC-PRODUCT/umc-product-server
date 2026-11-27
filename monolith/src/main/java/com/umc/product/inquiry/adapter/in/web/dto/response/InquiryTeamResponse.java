package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryTeamInfo;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryTeamResponse(
    @Schema(description = "null이면 어느 팀인지 모르겠어요", nullable = true)
    InquiryTeam team,
    String name,
    String description
) {
    public static InquiryTeamResponse from(InquiryTeamInfo info) {
        return new InquiryTeamResponse(info.team(), info.name(), info.description());
    }
}
