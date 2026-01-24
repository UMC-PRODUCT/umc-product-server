package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo.RecruitmentSectionInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<FormPageResponse> pages = buildPages(info);
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

    private static List<FormPageResponse> buildPages(RecruitmentApplicationFormInfo info) {
        RecruitmentFormDefinitionInfo rdef = info.recruitmentFormDefinition();

        if (rdef != null && rdef.sections() != null && !rdef.sections().isEmpty()) {
            return buildPagesFromRecruitmentDef(rdef);
        }

        FormDefinitionInfo def = info.formDefinition();
        if (def == null || def.sections() == null) {
            return List.of();
        }

        return def.sections().stream()
                .sorted(Comparator.comparing(FormDefinitionInfo.FormSectionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(section -> FormPageResponse.fromCommonSectionOrderNoAsPage(section))
                .toList();
    }

    private static List<FormPageResponse> buildPagesFromRecruitmentDef(RecruitmentFormDefinitionInfo rdef) {
        List<RecruitmentFormDefinitionInfo.RecruitmentSectionInfo> sections =
                rdef.sections() == null ? List.of() : rdef.sections();

        Map<Integer, List<RecruitmentSectionInfo>> commonByPage =
                sections.stream()
                        .filter(s -> s != null && s.kind()
                                == RecruitmentFormDefinitionInfo.RecruitmentSectionInfo.SectionKind.COMMON_PAGE)
                        .filter(s -> s.pageNo() != null)
                        .collect(Collectors.groupingBy(RecruitmentFormDefinitionInfo.RecruitmentSectionInfo::pageNo));

        Map<ChallengerPart, List<FormDefinitionInfo.QuestionInfo>> partQuestions =
                sections.stream()
                        .filter(s -> s != null
                                && s.kind() == RecruitmentFormDefinitionInfo.RecruitmentSectionInfo.SectionKind.PART)
                        .filter(s -> s.part() != null)
                        .collect(Collectors.groupingBy(
                                RecruitmentFormDefinitionInfo.RecruitmentSectionInfo::part,
                                Collectors.flatMapping(
                                        s -> (s.questions() == null ? List.<FormDefinitionInfo.QuestionInfo>of()
                                                : s.questions()).stream(),
                                        Collectors.toList()
                                )
                        ));

        List<Integer> orderedPages = new ArrayList<>(commonByPage.keySet());
        orderedPages.sort(Integer::compareTo);

        List<FormPageResponse> result = new ArrayList<>();

        for (Integer pageNo : orderedPages) {
            List<FormDefinitionInfo.QuestionInfo> commonQs = commonByPage.getOrDefault(pageNo, List.of()).stream()
                    .flatMap(s -> (s.questions() == null ? List.<FormDefinitionInfo.QuestionInfo>of()
                            : s.questions()).stream())
                    .sorted(Comparator.comparing(FormDefinitionInfo.QuestionInfo::orderNo,
                            Comparator.nullsLast(Integer::compareTo)))
                    .toList();

            FormDefinitionInfo.QuestionInfo scheduleQ = commonQs.stream()
                    .filter(q -> q != null && q.type() == QuestionType.SCHEDULE)
                    .findFirst()
                    .orElse(null);

            List<FormDefinitionInfo.QuestionInfo> normalCommon = commonQs.stream()
                    .filter(q -> q != null && q.type() != QuestionType.SCHEDULE)
                    .toList();

            ScheduleQuestionResponse schedule = (scheduleQ == null)
                    ? null
                    : ScheduleQuestionResponse.from(QuestionResponse.from(scheduleQ), null); // schedule은 추후 채움

            List<PartQuestionGroupResponse> partGroups = partQuestions.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
                    .map(e -> {
                        List<QuestionResponse> qs = e.getValue().stream()
                                .sorted(Comparator.comparing(FormDefinitionInfo.QuestionInfo::orderNo,
                                        Comparator.nullsLast(Integer::compareTo)))
                                .map(QuestionResponse::from)
                                .toList();
                        return new PartQuestionGroupResponse(e.getKey(), qs);
                    })
                    .toList();

            result.add(new FormPageResponse(
                    pageNo,
                    normalCommon.stream().map(QuestionResponse::from).toList(),
                    schedule,
                    partGroups.isEmpty() ? null : partGroups
            ));
        }

        return result;
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

        public static FormPageResponse fromCommonSectionOrderNoAsPage(FormDefinitionInfo.FormSectionInfo section) {
            List<QuestionResponse> ordered = (section.questions() == null ? List.<FormDefinitionInfo.QuestionInfo>of()
                    : section.questions()).stream()
                    .sorted(Comparator.comparing(FormDefinitionInfo.QuestionInfo::orderNo,
                            Comparator.nullsLast(Integer::compareTo)))
                    .map(QuestionResponse::from)
                    .toList();

            ScheduleQuestionResponse schedule = ordered.stream()
                    .filter(q -> q.type == QuestionType.SCHEDULE)
                    .findFirst()
                    .map(q -> ScheduleQuestionResponse.from(q, null))
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
            QuestionType type,
            String questionText,
            boolean required,
            List<OptionResponse> options
    ) {
        public static QuestionResponse from(FormDefinitionInfo.QuestionInfo q) {
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
