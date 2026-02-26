package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateRecruitmentInfo(
    Long recruitmentId,
    Long formId
) {
    public static CreateRecruitmentInfo of(Long recruitmentId, Long formId) {
        return new CreateRecruitmentInfo(recruitmentId, formId);
    }
}
