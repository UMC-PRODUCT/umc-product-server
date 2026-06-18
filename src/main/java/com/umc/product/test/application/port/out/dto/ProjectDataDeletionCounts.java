package com.umc.product.test.application.port.out.dto;

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
    int deletedSurveyForms,
    int deletedSurveyFormSections,
    int deletedSurveyQuestions,
    int deletedSurveyQuestionOptions,
    int deletedSurveyFormResponses,
    int deletedSurveyAnswers,
    int deletedSurveyAnswerChoices,
    int deletedSurveySingleAnswers
) {
}
