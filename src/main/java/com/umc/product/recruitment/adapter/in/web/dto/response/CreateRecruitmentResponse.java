package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;

public record CreateRecruitmentResponse(
        Long recruitmentId,
        Long formId
) {
    public static CreateRecruitmentResponse from(CreateRecruitmentInfo info) {
        return new CreateRecruitmentResponse(
                info.recruitmentId(),
                info.formId()
        );
    }

}
