package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;

import lombok.Builder;

/**
 * 테스트 프로젝트 데이터 삭제 결과.
 */
@Builder
public record DeleteSeedProjectDataResult(
    Long gisuId,
    int deletedProjects,
    int deletedProjectMembers,
    int deletedProjectApplications,
    int deletedProjectApplicationForms,
    int deletedProjectApplicationFormPolicies,
    int deletedProjectPartQuotas,
    int deletedProjectMatchingRounds,
    int deletedSurveyForms,
    int deletedSurveyFormSections,
    int deletedSurveyQuestions,
    int deletedSurveyQuestionOptions,
    int deletedSurveyFormResponses,
    int deletedSurveyAnswers,
    int deletedSurveyAnswerChoices,
    int deletedSurveySingleAnswers
) {

    public static DeleteSeedProjectDataResult from(Long gisuId, ProjectDataDeletionCounts counts) {
        return DeleteSeedProjectDataResult.builder()
            .gisuId(gisuId)
            .deletedProjects(counts.deletedProjects())
            .deletedProjectMembers(counts.deletedProjectMembers())
            .deletedProjectApplications(counts.deletedProjectApplications())
            .deletedProjectApplicationForms(counts.deletedProjectApplicationForms())
            .deletedProjectApplicationFormPolicies(counts.deletedProjectApplicationFormPolicies())
            .deletedProjectPartQuotas(counts.deletedProjectPartQuotas())
            .deletedProjectMatchingRounds(counts.deletedProjectMatchingRounds())
            .deletedSurveyForms(counts.deletedSurveyForms())
            .deletedSurveyFormSections(counts.deletedSurveyFormSections())
            .deletedSurveyQuestions(counts.deletedSurveyQuestions())
            .deletedSurveyQuestionOptions(counts.deletedSurveyQuestionOptions())
            .deletedSurveyFormResponses(counts.deletedSurveyFormResponses())
            .deletedSurveyAnswers(counts.deletedSurveyAnswers())
            .deletedSurveyAnswerChoices(counts.deletedSurveyAnswerChoices())
            .deletedSurveySingleAnswers(counts.deletedSurveySingleAnswers())
            .build();
    }
}
