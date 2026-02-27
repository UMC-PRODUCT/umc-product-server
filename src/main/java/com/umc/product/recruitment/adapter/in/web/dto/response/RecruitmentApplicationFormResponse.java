package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * 지원서 작성 페이지에 해당 학교/파트의 폼을 불러오는 용도로 사용됩니다. (운영진이 모집 폼 작성 중 미리보기로 확인했다면 status에 DRAFT가 들어갑니다)
 */
public record RecruitmentApplicationFormResponse(
    Long recruitmentId,
    Long formId,
    String status,
    String recruitmentFormTitle,
    String noticeTitle,
    String noticeContent,
    List<FormPageResponse> pages
) {
    public static RecruitmentApplicationFormResponse from(RecruitmentApplicationFormInfo info,
                                                          List<FormPageResponse> pages) {
        return new RecruitmentApplicationFormResponse(
            info.recruitmentId(),
            info.formId(),
            info.status(),
            info.recruitmentFormTitle(),
            info.noticeTitle(),
            info.noticeContent(),
            pages
        );
    }

    /**
     * page (UI) == section.orderNo (백엔드)
     */
    public record FormPageResponse(
        Integer page,
        List<QuestionResponse> questions,
        ScheduleQuestionResponse scheduleQuestion,
        List<PartQuestionGroupResponse> partQuestions
    ) {

        public static FormPageResponse fromCommonSectionOrderNoAsPage(
            FormDefinitionInfo.FormSectionInfo section,
            InterviewTimeTablePayloadResponse schedulePayload,
            RecruitmentApplicationFormInfo.PreferredPartInfo preferredPartInfo
        ) {
            List<QuestionResponse> ordered = (section.questions() == null
                ? List.<FormDefinitionInfo.QuestionInfo>of()
                : section.questions()).stream()
                .sorted(Comparator.comparing(
                    FormDefinitionInfo.QuestionInfo::orderNo,
                    Comparator.nullsLast(Integer::compareTo)
                ))
                .map(q -> QuestionResponse.from(q, preferredPartInfo))
                .toList();

            ScheduleQuestionResponse schedule = ordered.stream()
                .filter(q -> q.type == QuestionType.SCHEDULE)
                .findFirst()
                .map(q -> ScheduleQuestionResponse.from(q, schedulePayload))
                .orElse(null);

            List<QuestionResponse> normalQuestions = ordered.stream()
                .filter(q -> q.type != QuestionType.SCHEDULE)
                .toList();

            Integer page = section.orderNo() == null ? -1 : section.orderNo();

            return new FormPageResponse(page, normalQuestions, schedule, null);
        }

        public static FormPageResponse from(FormDefinitionInfo.FormSectionInfo section) {
            List<QuestionResponse> ordered = section.questions().stream()
                .sorted(Comparator.comparing(
                    FormDefinitionInfo.QuestionInfo::orderNo,
                    Comparator.nullsLast(Integer::compareTo)
                ))
                .map(QuestionResponse::from)
                .toList();

            // 타임테이블용 SCHEDULE 질문
            ScheduleQuestionResponse schedule = ordered.stream()
                .filter(q -> q.type == QuestionType.SCHEDULE)
                .findFirst()
                .map(q -> ScheduleQuestionResponse.from(q, null)) // schedule은 추후 실 구현 시 채움
                .orElse(null);

            // SCHEDULE 질문은 일반 questions에서 제거(중복 방지)
            List<QuestionResponse> normalQuestions = ordered.stream()
                .filter(q -> q.type != QuestionType.SCHEDULE)
                .toList();

            int page = section.orderNo() == null ? -1 : section.orderNo(); // 임시로 part면 orderNo 안내려주는 방식으로 구현

            return new FormPageResponse(
                page,
                normalQuestions,
                schedule,
                null // partQuestions: 실 구현 시 추가
            );
        }
    }

    public record QuestionResponse(
        Long questionId,
        Integer orderNo,
        QuestionType type,
        String questionText,
        boolean required,
        List<OptionResponse> options,
        Integer maxSelectCount,
        List<PreferredPartOptionResponse> preferredPartOptions
    ) {
        public static QuestionResponse from(FormDefinitionInfo.QuestionInfo q) {
            return from(q, null);
        }

        public static QuestionResponse from(FormDefinitionInfo.QuestionInfo q,
                                            RecruitmentApplicationFormInfo.PreferredPartInfo preferredPartInfo) {

            if (q.type() == QuestionType.PREFERRED_PART) {
                return new QuestionResponse(
                    q.questionId(),
                    q.orderNo(),
                    q.type(),
                    q.questionText(),
                    q.isRequired(),
                    null,
                    preferredPartInfo == null ? null : preferredPartInfo.maxSelectCount(),
                    preferredPartInfo == null ? null
                        : preferredPartInfo.options().stream()
                            .map(PreferredPartOptionResponse::from)
                            .toList()
                );
            }

            List<OptionResponse> opts = (q.options() == null || q.options().isEmpty())
                ? null
                : q.options().stream()
                    .sorted(Comparator.comparing(
                        FormDefinitionInfo.QuestionOptionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)
                    ))
                    .map(OptionResponse::from)
                    .toList();

            return new QuestionResponse(
                q.questionId(),
                q.orderNo(),
                q.type(),
                q.questionText(),
                q.isRequired(),
                opts,
                null,
                null
            );
        }
    }

    public record PreferredPartOptionResponse(
        Long recruitmentPartId,
        String label,
        String value
    ) {
        public static PreferredPartOptionResponse from(
            RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo o
        ) {
            return new PreferredPartOptionResponse(o.recruitmentPartId(), o.label(), o.value());
        }
    }

    public record OptionResponse(
        Long optionId,
        Integer orderNo,
        String content,
        boolean isOther
    ) {
        public static OptionResponse from(FormDefinitionInfo.QuestionOptionInfo o) {
            return new OptionResponse(o.optionId(), o.orderNo(), o.content(), o.isOther());
        }
    }

    public record ScheduleQuestionResponse(
        Long questionId,
        Integer orderNo,
        QuestionType type,
        String questionText,
        boolean required,
        InterviewTimeTablePayloadResponse schedule
    ) {
        public static ScheduleQuestionResponse from(QuestionResponse base, InterviewTimeTablePayloadResponse schedule) {
            return new ScheduleQuestionResponse(
                base.questionId(),
                base.orderNo(),
                base.type(),
                base.questionText(),
                base.required(),
                schedule
            );
        }
    }

    public record InterviewTimeTablePayloadResponse(
        DateRangeResponse dateRange,
        TimeRangeResponse timeRange,
        int slotMinutes,
        List<DateScheduleTimesResponse> enabled,
        List<DateScheduleTimesResponse> disabled
    ) {
    }

    public record DateRangeResponse(LocalDate start, LocalDate end) {
    }

    public record TimeRangeResponse(String start, String end) {
    }

    /**
     * 같은 날짜는 times 배열로 묶어서 내려주는 구조 e.g. { "date": "2026-01-06", "times": ["16:00", "16:30", "17:00"] }
     */
    public record DateScheduleTimesResponse(
        LocalDate date,
        List<String> times
    ) {
    }

    // ===== partQuestions =====
    public record PartQuestionGroupResponse(
        ChallengerPart part,
        List<QuestionResponse> questions
    ) {
    }

}
