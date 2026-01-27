package com.umc.product.recruitment.application.port.in.command.dto;

import java.util.Map;

public record UpdateRecruitmentInterviewPreferenceInfo(
        Long formResponseId,
        Map<String, Object> value
) {
    public static UpdateRecruitmentInterviewPreferenceInfo of(Long formResponseId, Map<String, Object> value) {
        return new UpdateRecruitmentInterviewPreferenceInfo(formResponseId, value);
    }
}
