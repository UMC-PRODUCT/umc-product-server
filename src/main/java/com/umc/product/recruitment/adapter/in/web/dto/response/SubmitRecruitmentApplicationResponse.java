package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;

public record SubmitRecruitmentApplicationResponse(
    Long recruitmentId,
    Long formResponseId,
    Long applicationId,
    String status // "SUBMITTED"
) {
    public static SubmitRecruitmentApplicationResponse from(SubmitRecruitmentApplicationInfo info) {
        return new SubmitRecruitmentApplicationResponse(
            info.recruitmentId(),
            info.formResponseId(),
            info.applicationId(),
            info.status().name()
        );
    }
}
