package com.umc.product.survey.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SurveyErrorCode implements BaseCode {

    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-0001", "폼을 찾을 수 없습니다."),
    SURVEY_NOT_DRAFT(HttpStatus.CONFLICT, "SURVEY-0002", "임시저장 상태의 폼만 편집할 수 있습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-0003", "질문을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
