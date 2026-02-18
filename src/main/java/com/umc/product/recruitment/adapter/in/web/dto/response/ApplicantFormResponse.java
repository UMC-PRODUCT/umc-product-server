package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.FormPageResponse;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import java.util.List;

public record ApplicantFormResponse(
    Long recruitmentId,
    Long formId,
    String status,
    boolean canApply,
    String recruitmentFormTitle,
    String noticeTitle,
    String noticeContent,
    List<RecruitmentApplicationFormResponse.FormPageResponse> pages
) {
    public static ApplicantFormResponse from(RecruitmentApplicationFormInfo info,
                                             boolean canApply,
                                             List<FormPageResponse> pages) {
        return new ApplicantFormResponse(
            info.recruitmentId(),
            info.formId(),
            info.status(),
            canApply,
            info.recruitmentFormTitle(),
            info.noticeTitle(),
            info.noticeContent(),
            pages
        );
    }
}
