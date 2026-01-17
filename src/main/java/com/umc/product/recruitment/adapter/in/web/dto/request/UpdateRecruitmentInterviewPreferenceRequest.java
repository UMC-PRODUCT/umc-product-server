package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UpdateRecruitmentInterviewPreferenceRequest(
        @NotNull Map<String, Object> value
) {
}
