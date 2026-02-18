package com.umc.product.recruitment.adapter.in.web.mapper;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.DateRangeResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.DateScheduleTimesResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.FormPageResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.InterviewTimeTablePayloadResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.PartQuestionGroupResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.QuestionResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.ScheduleQuestionResponse;
import com.umc.product.recruitment.adapter.in.web.dto.response.RecruitmentApplicationFormResponse.TimeRangeResponse;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo.InterviewTimeTableInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo.RecruitmentSectionInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecruitmentFormMapper {

    public List<FormPageResponse> mapToPages(RecruitmentApplicationFormInfo info) {
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

        InterviewTimeTablePayloadResponse schedulePayload = toInterviewTimeTablePayloadResponse(
            info.interviewTimeTableInfo());

        return def.sections().stream()
            .sorted(Comparator.comparing(FormDefinitionInfo.FormSectionInfo::orderNo,
                Comparator.nullsLast(Integer::compareTo)))
            .map(section -> {
                var preferredPartInfo = info.preferredPartInfo();
                return FormPageResponse.fromCommonSectionOrderNoAsPage(
                    section,
                    schedulePayload,
                    preferredPartInfo
                );
            })
            .toList();
    }

    private List<FormPageResponse> buildPagesFromRecruitmentDef(
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

    public InterviewTimeTablePayloadResponse toInterviewTimeTablePayloadResponse(InterviewTimeTableInfo info) {
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
                        : x.times().stream().map(this::formatTime)
                            .toList()
                ))
                .toList();

        List<DateScheduleTimesResponse> disabled = (info.disabledByDate() == null) ? null
            : info.disabledByDate().stream()
                .map(x -> new DateScheduleTimesResponse(
                    x.date(),
                    (x.times() == null) ? List.of()
                        : x.times().stream().map(this::formatTime)
                            .toList()
                ))
                .toList();

        int slotMinutes = (info.slotMinutes() == null) ? 0 : info.slotMinutes();

        return new InterviewTimeTablePayloadResponse(dr, tr, slotMinutes, enabled, disabled);
    }

    private String formatTime(LocalTime t) {
        if (t == null) {
            return null;
        }
        return t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
