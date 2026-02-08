package com.umc.product.recruitment.adapter.in.web.mapper;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.domain.Member;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.service.AnswerDisplayTextResolver;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationDetailMapper {

    private final AnswerDisplayTextResolver answerDisplayTextResolver;

    public ApplicationDetailInfo map(
        Application application,
        Member applicant,
        FormDefinitionInfo def,
        RecruitmentFormDefinitionInfo rdef,
        List<AnswerInfo> answers,
        List<ApplicationPartPreference> prefs
    ) {
        var applicantInfo = new ApplicationDetailInfo.ApplicantInfo(
            applicant.getId(),
            applicant.getName(),
            applicant.getNickname()
        );

        Map<Long, AnswerInfo> answerByQid = (answers == null ? List.<AnswerInfo>of() : answers).stream()
            .filter(a -> a != null && a.questionId() != null)
            .collect(Collectors.toMap(AnswerInfo::questionId, a -> a, (a, b) -> a));

        Map<Long, FormDefinitionInfo.FormSectionInfo> sectionById =
            (def == null || def.sections() == null) ? Map.of()
                : def.sections().stream()
                    .filter(s -> s != null && s.sectionId() != null)
                    .collect(Collectors.toMap(
                        FormDefinitionInfo.FormSectionInfo::sectionId,
                        s -> s,
                        (a, b) -> a
                    ));

        List<RecruitmentFormDefinitionInfo.RecruitmentSectionInfo> sections =
            (rdef == null || rdef.sections() == null) ? List.of() : rdef.sections();

        // 1) 공통 질문 (pageNo -> questions)
        Map<Integer, List<FormDefinitionInfo.QuestionInfo>> commonQuestionsByPageNo =
            sections.stream()
                .filter(s -> s != null
                    && s.kind() == RecruitmentFormDefinitionInfo.RecruitmentSectionInfo.SectionKind.COMMON_PAGE)
                .filter(s -> s.pageNo() != null)
                .collect(Collectors.groupingBy(
                    RecruitmentFormDefinitionInfo.RecruitmentSectionInfo::pageNo,
                    Collectors.flatMapping(
                        rs -> questionsOfSection(sectionById, rs.sectionId()),
                        Collectors.toList()
                    )
                ));

        int maxCommonPageNo = commonQuestionsByPageNo.keySet().stream().max(Integer::compareTo).orElse(-1);
        int partPageNo = maxCommonPageNo + 1;

        // 2) 지망 파트 (1지망이 앞)
        List<ChallengerPart> selectedParts = (prefs == null ? List.<ApplicationPartPreference>of() : prefs).stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(
                ApplicationPartPreference::getPriority,
                Comparator.nullsLast(Integer::compareTo)
            ))
            .map(p -> p.getRecruitmentPart() == null ? null : p.getRecruitmentPart().getPart())
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // 3) build pages: 공통 문항
        List<ApplicationDetailInfo.PageInfo> pages = new ArrayList<>();

        commonQuestionsByPageNo.keySet().stream().sorted(Integer::compareTo).forEach(pageNo -> {
            List<FormDefinitionInfo.QuestionInfo> qs = commonQuestionsByPageNo.getOrDefault(pageNo, List.of()).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                    FormDefinitionInfo.QuestionInfo::orderNo,
                    Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();

            pages.add(new ApplicationDetailInfo.PageInfo(
                pageNo,
                qs.stream().map(q -> toQuestionInfo(q, answerByQid)).toList(),
                null // partQuestions
            ));
        });

        // 4) part page: 파트별로
        if (!selectedParts.isEmpty()) {
            List<ApplicationDetailInfo.PartQuestionGroupInfo> partGroups = selectedParts.stream()
                .map(part -> {
                    List<FormDefinitionInfo.QuestionInfo> partQs = sections.stream()
                        .filter(s -> s != null
                            && s.kind() == RecruitmentFormDefinitionInfo.RecruitmentSectionInfo.SectionKind.PART)
                        .filter(s -> s.part() == part)
                        .sorted(Comparator.comparing(
                            RecruitmentFormDefinitionInfo.RecruitmentSectionInfo::orderNo,
                            Comparator.nullsLast(Integer::compareTo)
                        ))
                        .flatMap(rs -> questionsOfSection(sectionById, rs.sectionId()))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(
                            FormDefinitionInfo.QuestionInfo::orderNo,
                            Comparator.nullsLast(Integer::compareTo)
                        ))
                        .toList();

                    List<ApplicationDetailInfo.QuestionInfo> mapped =
                        partQs.stream().map(q -> toQuestionInfo(q, answerByQid)).toList();

                    return mapped.isEmpty()
                        ? null
                        : new ApplicationDetailInfo.PartQuestionGroupInfo(part, mapped);
                })
                .filter(Objects::nonNull)
                .toList();

            if (!partGroups.isEmpty()) {
                pages.add(new ApplicationDetailInfo.PageInfo(
                    partPageNo,
                    List.of(),
                    partGroups
                ));
            }
        }

        return new ApplicationDetailInfo(
            application.getId(),
            application.getStatus(),
            applicantInfo,
            pages
        );
    }

    private Stream<FormDefinitionInfo.QuestionInfo> questionsOfSection(
        Map<Long, FormDefinitionInfo.FormSectionInfo> sectionById,
        Long sectionId
    ) {
        if (sectionId == null) {
            return Stream.empty();
        }
        FormDefinitionInfo.FormSectionInfo sec = sectionById.get(sectionId);
        if (sec == null || sec.questions() == null) {
            return Stream.empty();
        }
        return sec.questions().stream().filter(Objects::nonNull);
    }

    private ApplicationDetailInfo.QuestionInfo toQuestionInfo(
        FormDefinitionInfo.QuestionInfo q,
        Map<Long, AnswerInfo> answerByQid
    ) {
        List<ApplicationDetailInfo.OptionInfo> options =
            (q.options() == null ? List.<FormDefinitionInfo.QuestionOptionInfo>of() : q.options()).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                    FormDefinitionInfo.QuestionOptionInfo::orderNo,
                    Comparator.nullsLast(Integer::compareTo)
                ))
                .map(o -> new ApplicationDetailInfo.OptionInfo(o.optionId(), o.content(), o.isOther()))
                .toList();

        AnswerInfo a = answerByQid.get(q.questionId());
        ApplicationDetailInfo.AnswerInfo answerInfo = null;

        if (a != null) {
            Object rawValue = a.value();
            String displayText = answerDisplayTextResolver.resolve(q.type(), options, rawValue);

            answerInfo = new ApplicationDetailInfo.AnswerInfo(
                a.answeredAsType(),
                displayText,
                rawValue
            );
        }

        return new ApplicationDetailInfo.QuestionInfo(
            q.questionId(),
            q.orderNo(),
            q.type(),
            q.questionText(),
            q.isRequired(),
            options,
            answerInfo
        );
    }
}
