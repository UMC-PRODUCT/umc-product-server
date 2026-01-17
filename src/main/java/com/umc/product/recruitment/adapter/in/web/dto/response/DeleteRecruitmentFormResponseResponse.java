package com.umc.product.recruitment.adapter.in.web.dto.response;

public record DeleteRecruitmentFormResponseResponse(
        Long formResponseId
) {
    public static DeleteRecruitmentFormResponseResponse of(Long formResponseId) {
        return new DeleteRecruitmentFormResponseResponse(formResponseId);
    }
}
