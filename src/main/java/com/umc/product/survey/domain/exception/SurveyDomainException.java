package com.umc.product.survey.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class SurveyDomainException extends BusinessException {
    public SurveyDomainException(SurveyErrorCode surveyErrorCode) {
        super(Domain.SURVEY, surveyErrorCode);
    }


}
