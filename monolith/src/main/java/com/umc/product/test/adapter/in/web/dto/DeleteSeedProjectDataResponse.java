package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataResult;

public record DeleteSeedProjectDataResponse(
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

    public static DeleteSeedProjectDataResponse from(DeleteSeedProjectDataResult result) {
        return new DeleteSeedProjectDataResponse(
            result.gisuId(),
            result.deletedProjects(),
            result.deletedProjectMembers(),
            result.deletedProjectApplications(),
            result.deletedProjectApplicationForms(),
            result.deletedProjectApplicationFormPolicies(),
            result.deletedProjectPartQuotas(),
            result.deletedProjectMatchingRounds(),
            result.deletedSurveyForms(),
            result.deletedSurveyFormSections(),
            result.deletedSurveyQuestions(),
            result.deletedSurveyQuestionOptions(),
            result.deletedSurveyFormResponses(),
            result.deletedSurveyAnswers(),
            result.deletedSurveyAnswerChoices(),
            result.deletedSurveySingleAnswers()
        );
    }
}
