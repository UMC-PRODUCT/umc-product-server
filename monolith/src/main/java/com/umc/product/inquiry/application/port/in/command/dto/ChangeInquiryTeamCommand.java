package com.umc.product.inquiry.application.port.in.command.dto;

import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * @param targetTeam null이면 "어느 팀인지 모르겠어요"다.
 */
public record ChangeInquiryTeamCommand(
    Long inquiryId,
    Long actorMemberId,
    InquiryTeam targetTeam
) {
}
