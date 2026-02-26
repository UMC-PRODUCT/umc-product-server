package com.umc.product.recruitment.application.port.in.command.dto;

import java.util.Map;

public record UpdateRecruitmentInterviewPreferenceCommand(
    Long memberId,
    Long recruitmentId,
    Long formResponseId,
    Map<String, Object> value
) {
}
