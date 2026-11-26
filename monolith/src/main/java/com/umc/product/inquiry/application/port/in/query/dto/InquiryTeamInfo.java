package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * @param team null이면 "어느 팀인지 모르겠어요"다.
 */
public record InquiryTeamInfo(
    InquiryTeam team,
    String name,
    String description
) {
    public static InquiryTeamInfo from(InquiryTeam team) {
        return new InquiryTeamInfo(team, team.getDisplayName(), team.getDescription());
    }

    public static InquiryTeamInfo unknown() {
        return new InquiryTeamInfo(null, InquiryTeam.UNKNOWN_DISPLAY_NAME, InquiryTeam.UNKNOWN_DESCRIPTION);
    }
}
