package com.umc.product.recruitment.adapter.in.web.dto.response;

public record ActiveRecruitmentIdResponse(
    Long recruitmentId
) {
    public static ActiveRecruitmentIdResponse of(Long recruitmentId) {
        return new ActiveRecruitmentIdResponse(recruitmentId);
    }
}
