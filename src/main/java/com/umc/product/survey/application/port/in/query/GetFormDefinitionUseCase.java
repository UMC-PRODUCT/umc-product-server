package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.in.query.dto.GetFormDefinitionQuery;

public interface GetFormDefinitionUseCase {
    FormDefinitionInfo getFormDefinition(GetFormDefinitionQuery query);
}
