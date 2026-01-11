package com.umc.product.survey.application.port.in.query;

public interface GetFormDefinitionUseCase {
    FormDefinitionInfo getFormDefinition(GetFormDefinitionQuery query);
}
