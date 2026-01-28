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
    SURVEY_ALREADY_PUBLISHED(HttpStatus.BAD_REQUEST, "SURVEY-005", "이미 발행된 폼입니다."),
    FORM_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-0006", "폼 응답을 찾을 수 없습니다."),
    QUESTION_IS_NOT_OWNED_BY_FORM(HttpStatus.BAD_REQUEST, "SURVEY-0007", "질문이 해당 폼의 질문이 아닙니다."),
    FORM_RESPONSE_FORBIDDEN(HttpStatus.FORBIDDEN, "SURVEY-0008", "해당 폼 응답에 접근할 수 있는 권한이 없습니다."),
    QUESTION_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "SURVEY-0009", "질문 유형이 일치하지 않습니다."),
    REQUIRED_QUESTION_NOT_ANSWERED(HttpStatus.BAD_REQUEST, "SURVEY-0010", "필수 질문에 대한 응답이 누락되었습니다."),
    INVALID_ANSWER_FORMAT(HttpStatus.BAD_REQUEST, "SURVEY-0011", "응답 형식이 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
