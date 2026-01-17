package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.LocalDate;
import java.time.LocalTime;
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
    public static RecruitmentApplicationFormResponse from(RecruitmentApplicationFormInfo info) {
        FormDefinitionInfo def = info.formDefinition();

        return new RecruitmentApplicationFormResponse(
                info.recruitmentId(),
                info.formId(),
                info.status(),
                info.recruitmentFormTitle(),
                info.noticeTitle(),
                info.noticeContent(),
                def.sections().stream()
                        .sorted(Comparator.comparingInt(FormDefinitionInfo.FormSectionInfo::orderNo))
                        .map(FormPageResponse::from)
                        .toList()
        );
    }

    /**
     * page (UI) == section.orderNo (백엔드)
     */
    public record FormPageResponse(
            int page,
            List<QuestionResponse> questions,
            ScheduleQuestionResponse scheduleQuestion,
            List<PartQuestionGroupResponse> partQuestions
    ) {
        public static FormPageResponse from(FormDefinitionInfo.FormSectionInfo section) {
            List<QuestionResponse> ordered = section.questions().stream()
                    .sorted(Comparator.comparingInt(FormDefinitionInfo.QuestionInfo::orderNo))
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

            return new FormPageResponse(
                    section.orderNo(),
                    normalQuestions,
                    schedule,
                    null // partQuestions: 실 구현 시 추가
            );
        }
    }

    public record QuestionResponse(
            Long questionId,
            QuestionType type,
            String questionText,
            boolean required,
            List<OptionResponse> options
    ) {
        public static QuestionResponse from(FormDefinitionInfo.QuestionInfo q) {
            List<OptionResponse> opts = (q.options() == null || q.options().isEmpty())
                    ? null
                    : q.options().stream()
                            .sorted(Comparator.comparingInt(FormDefinitionInfo.QuestionOptionInfo::orderNo))
                            .map(OptionResponse::from)
                            .toList();

            return new QuestionResponse(
                    q.questionId(),
                    q.type(),
                    q.questionText(),
                    q.isRequired(),
                    opts
            );
        }
    }

    public record OptionResponse(
            Long optionId,
            String content
    ) {
        public static OptionResponse from(FormDefinitionInfo.QuestionOptionInfo o) {
            return new OptionResponse(o.optionId(), o.content());
        }
    }

    public record ScheduleQuestionResponse(
            Long questionId,
            QuestionType type,
            String questionText,
            boolean required,
            ScheduleResponse schedule
    ) {
        public static ScheduleQuestionResponse from(QuestionResponse base, ScheduleResponse schedule) {
            return new ScheduleQuestionResponse(
                    base.questionId(),
                    base.type(),
                    base.questionText(),
                    base.required(),
                    schedule
            );
        }
    }

    public record ScheduleResponse(
            DateRangeResponse dateRange,
            TimeRangeResponse timeRange,
            int slotMinutes,
            List<DateScheduleTimesResponse> enabled,
            List<DateScheduleTimesResponse> disabled
    ) {
    }

    public record DateRangeResponse(LocalDate start, LocalDate end) {
    }

    public record TimeRangeResponse(LocalTime start, LocalTime end) {
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
