package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.survey.domain.enums.FormResponseStatus;

public record SubmitRecruitmentApplicationInfo(
    Long recruitmentId,
    Long formResponseId,
    Long applicationId,
    FormResponseStatus status
) {
    public static SubmitRecruitmentApplicationInfo of(Long recruitmentId, Long formResponseId, Long applicationId,
                                                      FormResponseStatus status) {
        return new SubmitRecruitmentApplicationInfo(
            recruitmentId,
            formResponseId,
            applicationId,
            FormResponseStatus.SUBMITTED
        );
    }
}
