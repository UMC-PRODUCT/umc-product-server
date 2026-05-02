package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.enums.FormSectionType;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;

/**
 * 지원 폼 전체 구조 (폼 메타 + 섹션 → 질문 → 옵션) Info DTO.
 * <p>
 * Survey 도메인의 {@link FormWithStructureInfo} 와 Project 도메인의 정책({@link ProjectApplicationFormPolicy})
 * 을 합성하여 단일 응답 구조를 만든다.
 */
@Builder
public record ApplicationFormInfo(
    Long projectId,
    Long applicationFormId,
    String title,
    String description,
    List<SectionInfo> sections
) {

    /**
     * 어댑터/서비스에서 모은 데이터들을 합성하여 Info DTO 를 조립한다.
     */
    public static ApplicationFormInfo of(
        ProjectApplicationForm applicationForm,
        FormWithStructureInfo formStructure,
        List<ProjectApplicationFormPolicy> policies
    ) {
        Map<Long, ProjectApplicationFormPolicy> policyByFormSectionId = policies.stream()
            .collect(Collectors.toMap(ProjectApplicationFormPolicy::getFormSectionId, Function.identity()));

        List<SectionInfo> sections = formStructure.sections().stream()
            .map(section -> SectionInfo.of(section, policyByFormSectionId.get(section.sectionId())))
            .toList();

        return ApplicationFormInfo.builder()
            .projectId(applicationForm.getProject().getId())
            .applicationFormId(applicationForm.getId())
            .title(formStructure.title())
            .description(formStructure.description())
            .sections(sections)
            .build();
    }

    /**
     * 섹션 정보. 정책이 없는 섹션(보통은 발생하지 않지만 데이터 정합 깨질 경우)은 PART + 빈 parts 로 폴백한다.
     */
    @Builder
    public record SectionInfo(
        Long sectionId,
        FormSectionType type,
        Set<ChallengerPart> allowedParts,
        String title,
        String description,
        long orderNo,
        List<QuestionInfo> questions
    ) {
        public static SectionInfo of(
            FormWithStructureInfo.SectionWithQuestions section,
            ProjectApplicationFormPolicy policy
        ) {
            FormSectionType type = (policy != null) ? policy.getType() : FormSectionType.PART;
            Set<ChallengerPart> allowedParts = (policy != null)
                ? new HashSet<>(policy.getAllowedParts())
                : Set.of();

            List<QuestionInfo> questions = section.questions().stream()
                .map(QuestionInfo::from)
                .toList();

            return SectionInfo.builder()
                .sectionId(section.sectionId())
                .type(type)
                .allowedParts(allowedParts)
                .title(section.title())
                .description(section.description())
                .orderNo(section.orderNo())
                .questions(questions)
                .build();
        }
    }

    @Builder
    public record QuestionInfo(
        Long questionId,
        QuestionType type,
        String title,
        String description,
        boolean isRequired,
        long orderNo,
        List<OptionInfo> options
    ) {
        public static QuestionInfo from(FormWithStructureInfo.QuestionWithOptions question) {
            List<OptionInfo> options = question.options().stream()
                .map(OptionInfo::from)
                .toList();

            return QuestionInfo.builder()
                .questionId(question.questionId())
                .type(question.type())
                .title(question.title())
                .description(question.description())
                .isRequired(question.isRequired())
                .orderNo(question.orderNo())
                .options(options)
                .build();
        }
    }

    @Builder
    public record OptionInfo(
        Long optionId,
        String content,
        long orderNo,
        boolean isOther
    ) {
        public static OptionInfo from(FormWithStructureInfo.Option option) {
            return OptionInfo.builder()
                .optionId(option.optionId())
                .content(option.content())
                .orderNo(option.orderNo())
                .isOther(option.isOther())
                .build();
        }
    }
}
