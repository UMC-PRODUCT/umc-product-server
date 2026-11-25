package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.inquiry.domain.enums.InquiryHistoryType;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * 담당자 변경은 {@code previousAssignee}·{@code newAssignee}를, 팀 변경은 {@code previousTeam}·{@code newTeam}을 채운다.
 * 문의 접수({@code RECEIVED})는 작성자가 한 일로 첫 줄에 둔다.
 */
public record InquiryHistoryInfo(
    InquiryHistoryType type,
    InquiryStaffMemberInfo actor,
    InquiryStaffMemberInfo previousAssignee,
    InquiryStaffMemberInfo newAssignee,
    InquiryTeam previousTeam,
    InquiryTeam newTeam,
    Instant occurredAt
) {
}
