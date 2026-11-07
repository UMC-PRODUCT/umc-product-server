package com.umc.product.test.application.port.out.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProjectDataDeletionCounts(
    int deletedProjects,
    int deletedProjectMembers,
    int deletedProjectApplications,
    int deletedProjectApplicationForms,
    int deletedProjectApplicationFormPolicies,
    int deletedProjectPartQuotas,
    int deletedProjectMatchingRounds,
    List<Long> deletedProjectMatchingRoundIds,
    int deletedSurveyForms,
    int deletedSurveyFormSections,
    int deletedSurveyQuestions,
    int deletedSurveyQuestionOptions,
    int deletedSurveyFormResponses,
    int deletedSurveyAnswers,
    int deletedSurveyAnswerChoices,
    int deletedSurveySingleAnswers
) {

    public ProjectDataDeletionCounts {
        deletedProjectMatchingRoundIds = deletedProjectMatchingRoundIds == null
            ? List.of() : List.copyOf(deletedProjectMatchingRoundIds);
    }
}
