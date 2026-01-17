package com.umc.product.recruitment.adapter.in.web.dto.response;

import java.util.Map;

public record UpdateRecruitmentInterviewPreferenceResponse(
        Long formResponseId,
        Map<String, Object> value
) {
    public static UpdateRecruitmentInterviewPreferenceResponse of(Long formResponseId, Map<String, Object> value) {
        return new UpdateRecruitmentInterviewPreferenceResponse(formResponseId, value);
    }
}
