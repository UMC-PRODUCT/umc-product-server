package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo.Option;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo.QuestionWithOptions;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo.SectionWithQuestions;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormSectionPort;
import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormQueryService implements GetFormUseCase {

    private final LoadFormPort loadFormPort;
    private final LoadFormSectionPort loadFormSectionPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadQuestionOptionPort loadQuestionOptionPort;

    @Override
    public Optional<FormInfo> findById(Long formId) {
        return loadFormPort.findById(formId)
            .map(FormInfo::from);
    }

    @Override
    public FormInfo getById(Long formId) {
        return loadFormPort.findById(formId)
            .map(FormInfo::from)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    @Override
    public FormWithStructureInfo getFormWithStructure(Long formId) {
        // 1. 폼 메타 로드
        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        // 2. 섹션 로드 (orderNo asc)
        List<FormSection> sections = loadFormSectionPort.listByFormId(formId);
        Set<Long> sectionIds = sections.stream()
            .map(FormSection::getId)
            .collect(Collectors.toSet());

        // 3. 모든 섹션의 질문을 벌크 로드 (N+1 회피)
        List<Question> questions = loadQuestionPort.listBySectionIdIn(sectionIds);
        Set<Long> questionIds = questions.stream()
            .map(Question::getId)
            .collect(Collectors.toSet());

        // 4. 모든 질문의 선택지를 벌크 로드
        List<QuestionOption> options = loadQuestionOptionPort.listByQuestionIdIn(questionIds);

        // 5. 메모리 grouping — sectionId -> 질문 / questionId -> 옵션
        Map<Long, List<Question>> questionsBySection = questions.stream()
            .collect(Collectors.groupingBy(q -> q.getFormSection().getId()));
        Map<Long, List<QuestionOption>> optionsByQuestion = options.stream()
            .collect(Collectors.groupingBy(o -> o.getQuestion().getId()));

        // 6. 중첩 DTO 조립
        List<SectionWithQuestions> sectionDtos = sections.stream()
            .map(section -> SectionWithQuestions.builder()
                .sectionId(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .orderNo(section.getOrderNo())
                .questions(toQuestionDtos(
                    questionsBySection.getOrDefault(section.getId(), List.of()),
                    optionsByQuestion
                ))
                .build())
            .toList();

        return FormWithStructureInfo.builder()
            .formId(form.getId())
            .createdMemberId(form.getCreatedMemberId())
            .title(form.getTitle())
            .description(form.getDescription())
            .status(form.getStatus())
            .isAnonymous(form.isAnonymous())
            .createdAt(form.getCreatedAt())
            .updatedAt(form.getUpdatedAt())
            .sections(sectionDtos)
            .build();
    }

    private List<QuestionWithOptions> toQuestionDtos(
        List<Question> questions,
        Map<Long, List<QuestionOption>> optionsByQuestion
    ) {
        return questions.stream()
            .map(question -> QuestionWithOptions.builder()
                .questionId(question.getId())
                .title(question.getTitle())
                .description(question.getDescription())
                .type(question.getType())
                .isRequired(Boolean.TRUE.equals(question.getIsRequired()))
                .orderNo(question.getOrderNo())
                .options(toOptionDtos(
                    optionsByQuestion.getOrDefault(question.getId(), List.of())
                ))
                .build())
            .toList();
    }

    private List<Option> toOptionDtos(List<QuestionOption> options) {
        return options.stream()
            .map(option -> Option.builder()
                .optionId(option.getId())
                .content(option.getContent())
                .orderNo(option.getOrderNo())
                .isOther(option.isOther())
                .build())
            .toList();
    }
}
