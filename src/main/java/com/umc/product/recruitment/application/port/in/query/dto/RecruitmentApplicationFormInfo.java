package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record RecruitmentApplicationFormInfo(
    Long recruitmentId,
    Long formId,
    String status,
    String recruitmentFormTitle,
    String noticeTitle,
    String noticeContent,
    FormDefinitionInfo formDefinition,
    RecruitmentFormDefinitionInfo recruitmentFormDefinition,
    InterviewTimeTableInfo interviewTimeTableInfo,
    PreferredPartInfo preferredPartInfo
) {
    public record PreferredPartInfo(
        Integer maxSelectCount,
        List<PreferredPartOptionInfo> options
    ) {
        public record PreferredPartOptionInfo(
            Long recruitmentPartId,
            String label,
            String value
        ) {
        }
    }

    public record InterviewTimeTableInfo(
        DateRangeInfo dateRange,
        TimeRangeInfo timeRange, // applicant view용: enabled 전체 min/max로 계산된 값
        Integer slotMinutes,
        List<TimesByDateInfo> enabledByDate,
        List<TimesByDateInfo> disabledByDate
    ) {
        public record DateRangeInfo(LocalDate start, LocalDate end) {
        }

        public record TimeRangeInfo(LocalTime start, LocalTime end) {
        }

        public record TimesByDateInfo(LocalDate date, List<LocalTime> times) {
        }
    }

//    public static RecruitmentApplicationFormInfo from(
//            Recruitment recruitment,
//            FormDefinitionInfo formDefinition,
//            InterviewTimeTableInfo interviewTimetable
//    ) {
//        return new RecruitmentApplicationFormInfo(
//                recruitment.getId(),
//                recruitment.getFormId(),
//                recruitment.getStatus() == null ? null : recruitment.getStatus().name(),
//                recruitment.getTitle(),
//                recruitment.getNoticeTitle(),
//                recruitment.getNoticeContent(),
//                formDefinition,
//                null,
//                interviewTimetable
//        );
//    }

    public static RecruitmentApplicationFormInfo from(
        Recruitment recruitment,
        FormDefinitionInfo formDefinition,
        RecruitmentFormDefinitionInfo recruitmentFormDefinition,
        InterviewTimeTableInfo interviewTimeTableInfo,
        PreferredPartInfo preferredPartInfo
    ) {
        return new RecruitmentApplicationFormInfo(
            recruitment.getId(),
            recruitment.getFormId(),
            recruitment.getStatus() == null ? null : recruitment.getStatus().name(),
            recruitment.getTitle(),
            recruitment.getNoticeTitle(),
            recruitment.getNoticeContent(),
            formDefinition,
            recruitmentFormDefinition,
            interviewTimeTableInfo,
            preferredPartInfo
        );
    }

    public RecruitmentApplicationFormInfo filterPartQuestions(Set<ChallengerPart> openParts) {
        if (openParts == null || openParts.isEmpty() || recruitmentFormDefinition == null) {
            return this;
        }

        RecruitmentFormDefinitionInfo filtered =
            recruitmentFormDefinition.filterForApplicantOpenParts(openParts);

        return new RecruitmentApplicationFormInfo(
            recruitmentId,
            formId,
            status,
            recruitmentFormTitle,
            noticeTitle,
            noticeContent,
            formDefinition,
            filtered,
            interviewTimeTableInfo,
            preferredPartInfo
        );
    }
}
