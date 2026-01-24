package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;

public record RecruitmentApplicationFormInfo(
        Long recruitmentId,
        Long formId,
        String status,
        String recruitmentFormTitle,
        String noticeTitle,
        String noticeContent,
        FormDefinitionInfo formDefinition
        // ScheduleDefinitionInfo scheduleDefinitionInfo
        // todo: 추후 실 구현 시 주석 제거
) {

    public static RecruitmentApplicationFormInfo from(
            Recruitment recruitment,
            FormDefinitionInfo formDefinition
    ) {
        return new RecruitmentApplicationFormInfo(
                recruitment.getId(),
                recruitment.getFormId(),
                recruitment.getStatus() == null ? null : recruitment.getStatus().name(),
                recruitment.getTitle(),
                recruitment.getNoticeTitle(),
                recruitment.getNoticeContent(),
                formDefinition
        );
    }
}
