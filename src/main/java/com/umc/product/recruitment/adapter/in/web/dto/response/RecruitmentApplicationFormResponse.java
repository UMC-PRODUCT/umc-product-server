package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo.InterviewTimeTableInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo.RecruitmentSectionInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        FormDefinitionInfo def = info.formDefinition();

        if (rdef != null && rdef.sections() != null && !rdef.sections().isEmpty()) {
            if (def == null || def.sections() == null) {
                return List.of();
            }
            return buildPagesFromRecruitmentDef(info, def, rdef);
        }

        if (def == null || def.sections() == null) {
            return List.of();
        }

        return def.sections().stream()
            .sorted(Comparator.comparing(FormDefinitionInfo.FormSectionInfo::orderNo,
                Comparator.nullsLast(Integer::compareTo)))
            .map(section -> {
                var preferredPartInfo = info.preferredPartInfo();
                return FormPageResponse.fromCommonSectionOrderNoAsPage(
                    section,
                    info.interviewTimeTableInfo(),
                    preferredPartInfo
                );
            })
            .toList();
    }

    private static List<FormPageResponse> buildPagesFromRecruitmentDef(
        RecruitmentApplicationFormInfo info,
        FormDefinitionInfo def,
        RecruitmentFormDefinitionInfo rdef
    ) {
        List<RecruitmentSectionInfo> sections =
            rdef.sections() == null ? List.of() : rdef.sections();

        // sectionId -> FormSectionInfo 매핑
        Map<Long, FormDefinitionInfo.FormSectionInfo> sectionById =
            (def.sections() == null ? List.<FormDefinitionInfo.FormSectionInfo>of() : def.sections())
                .stream()
                .filter(s -> s != null && s.sectionId() != null)
                .collect(Collectors.toMap(
                    FormDefinitionInfo.FormSectionInfo::sectionId,
                    s -> s,
                    (a, b) -> a
                ));

        // 1) 공통 페이지 섹션들: pageNo 기준 그룹
        Map<Integer, List<RecruitmentSectionInfo>> commonByPage =
            sections.stream()
                .filter(s -> s != null && s.kind() == RecruitmentSectionInfo.SectionKind.COMMON_PAGE)
                .filter(s -> s.pageNo() != null)
                .collect(Collectors.groupingBy(RecruitmentSectionInfo::pageNo));

        // 2) 파트 섹션들: part 기준 그룹
        Map<ChallengerPart, List<RecruitmentSectionInfo>> partSections =
            sections.stream()
                .filter(s -> s != null && s.kind() == RecruitmentSectionInfo.SectionKind.PART)
                .filter(s -> s.part() != null)
                .collect(Collectors.groupingBy(RecruitmentSectionInfo::part));

        List<Integer> orderedPages = new ArrayList<>(commonByPage.keySet());
        orderedPages.sort(Integer::compareTo);

        List<FormPageResponse> result = new ArrayList<>();

        InterviewTimeTablePayloadResponse schedulePayload = toInterviewTimeTablePayloadResponse(
            info.interviewTimeTableInfo());
        var preferredPartInfo = info.preferredPartInfo();

        for (Integer pageNo : orderedPages) {
            // 공통 질문 합치기
            List<FormDefinitionInfo.QuestionInfo> commonQs =
                commonByPage.getOrDefault(pageNo, List.of()).stream()
                    .map(RecruitmentSectionInfo::sectionId)
                    .map(sectionById::get)
                    .filter(java.util.Objects::nonNull)
                    .flatMap(sec -> (sec.questions() == null
                        ? List.<FormDefinitionInfo.QuestionInfo>of()
                        : sec.questions()).stream())
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.comparing(
                        FormDefinitionInfo.QuestionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)
                    ))
                    .toList();

            FormDefinitionInfo.QuestionInfo scheduleQ = commonQs.stream()
                .filter(q -> q.type() == QuestionType.SCHEDULE)
                .findFirst()
                .orElse(null);

            List<FormDefinitionInfo.QuestionInfo> normalCommon = commonQs.stream()
                .filter(q -> q.type() != QuestionType.SCHEDULE)
                .toList();

            // 스케줄
            ScheduleQuestionResponse schedule = (scheduleQ == null)
                ? null
                : ScheduleQuestionResponse.from(QuestionResponse.from(scheduleQ, preferredPartInfo),
                    schedulePayload);

            result.add(new FormPageResponse(
                pageNo,
                normalCommon.stream().map(q -> QuestionResponse.from(q, preferredPartInfo)).toList(),
                schedule,
                null
            ));
        }

        List<PartQuestionGroupResponse> partGroups = partSections.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
            .map(e -> {
                ChallengerPart part = e.getKey();

                List<FormDefinitionInfo.QuestionInfo> partQs = e.getValue().stream()
                    .map(RecruitmentSectionInfo::sectionId)
                    .map(sectionById::get)
                    .filter(java.util.Objects::nonNull)
                    .flatMap(sec -> (sec.questions() == null
                        ? List.<FormDefinitionInfo.QuestionInfo>of()
                        : sec.questions()).stream())
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.comparing(
                        FormDefinitionInfo.QuestionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)
                    ))
                    .toList();

                List<QuestionResponse> qs = partQs.stream()
                    .map(q -> QuestionResponse.from(q, preferredPartInfo))
                    .toList();

                return new PartQuestionGroupResponse(part, qs);
            })
            .toList();

        result.add(new FormPageResponse(
            3,
            List.of(),
            null,
            partGroups.isEmpty() ? null : partGroups
        ));
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

        public static FormPageResponse fromCommonSectionOrderNoAsPage(
            FormDefinitionInfo.FormSectionInfo section,
            InterviewTimeTableInfo interviewTimeTable,
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

            InterviewTimeTablePayloadResponse schedulePayload = toInterviewTimeTablePayloadResponse(interviewTimeTable);

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

    private static InterviewTimeTablePayloadResponse toInterviewTimeTablePayloadResponse(InterviewTimeTableInfo info) {
        if (info == null) {
            return null;
        }

        DateRangeResponse dr = (info.dateRange() == null) ? null
            : new DateRangeResponse(info.dateRange().start(), info.dateRange().end());

        TimeRangeResponse tr = (info.timeRange() == null) ? null
            : new TimeRangeResponse(formatTime(info.timeRange().start()), formatTime(info.timeRange().end()));

        List<DateScheduleTimesResponse> enabled = (info.enabledByDate() == null) ? null
            : info.enabledByDate().stream()
                .map(x -> new DateScheduleTimesResponse(
                    x.date(),
                    (x.times() == null) ? List.of()
                        : x.times().stream().map(RecruitmentApplicationFormResponse::formatTime)
                            .toList()
                ))
                .toList();

        List<DateScheduleTimesResponse> disabled = (info.disabledByDate() == null) ? null
            : info.disabledByDate().stream()
                .map(x -> new DateScheduleTimesResponse(
                    x.date(),
                    (x.times() == null) ? List.of()
                        : x.times().stream().map(RecruitmentApplicationFormResponse::formatTime)
                            .toList()
                ))
                .toList();

        int slotMinutes = (info.slotMinutes() == null) ? 0 : info.slotMinutes();

        return new InterviewTimeTablePayloadResponse(dr, tr, slotMinutes, enabled, disabled);
    }

    private static String formatTime(LocalTime t) {
        if (t == null) {
            return null;
        }
        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
