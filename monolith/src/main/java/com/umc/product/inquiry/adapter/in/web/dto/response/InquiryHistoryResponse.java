package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryHistoryInfo;
import com.umc.product.inquiry.domain.enums.InquiryHistoryType;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryHistoryResponse(
    @Schema(description = "RECEIVED: 문의 접수, ASSIGNED·REASSIGNED·UNASSIGNED: 담당자 변경, TEAM_CHANGED: 받는 팀 변경")
    InquiryHistoryType type,
    @Schema(description = "바꾼 사람. RECEIVED는 작성자")
    InquiryStaffMemberResponse actor,
    InquiryStaffMemberResponse previousAssignee,
    InquiryStaffMemberResponse newAssignee,
    InquiryTeam previousTeam,
    InquiryTeam newTeam,
    Instant occurredAt
) {
    public static InquiryHistoryResponse from(InquiryHistoryInfo info) {
        return new InquiryHistoryResponse(
            info.type(),
            InquiryStaffMemberResponse.from(info.actor()),
            InquiryStaffMemberResponse.from(info.previousAssignee()),
            InquiryStaffMemberResponse.from(info.newAssignee()),
            info.previousTeam(),
            info.newTeam(),
            info.occurredAt()
        );
    }
}
