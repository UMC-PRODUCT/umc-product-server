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
    OTHER_OPTION_DUPLICATED(HttpStatus.BAD_REQUEST, "SURVEY-0012", "'기타' 선택지가 중복되었습니다."),
    OPTION_NOT_IN_QUESTION(HttpStatus.BAD_REQUEST, "SURVEY-0013", "선택지가 해당 질문의 선택지에 포함되지 않습니다."),
    OPTION_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "SURVEY-0014", "'기타' 선택지의 텍스트는 필수입니다."),
    INVALID_FORM_ACTIVE_PERIOD(HttpStatus.BAD_REQUEST, "SURVEY-0015", "폼의 응답 가능 기간이 올바르지 않습니다."),
    INVALID_VOTE_OPTION_COUNT(HttpStatus.BAD_REQUEST, "SURVEY-0016", "투표 항목은 2개 이상 5개 이하여야 합니다."),
    INVALID_VOTE_OPTION_CONTENT(HttpStatus.BAD_REQUEST, "SURVEY-0017", "투표 항목에 빈 값이 포함될 수 없습니다."),
    INVALID_VOTE_START_DATE(HttpStatus.BAD_REQUEST, "SURVEY-0018", "투표 시작일은 오늘부터 선택 가능합니다."),
    INVALID_VOTE_END_DATE(HttpStatus.BAD_REQUEST, "SURVEY-0019", "투표 마감일은 시작일 하루 뒤부터 선택 가능합니다."),
    VOTE_NOT_STARTED(HttpStatus.BAD_REQUEST, "SURVEY-0020", "아직 투표 기간이 아닙니다."),
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "SURVEY-0021", "이미 종료된 투표입니다."),
    VOTE_ALREADY_RESPONDED(HttpStatus.BAD_REQUEST, "SURVEY-0022", "이미 답변한 투표입니다."),
    INVALID_VOTE_SELECTION(HttpStatus.BAD_REQUEST, "SURVEY-0023", "선택이 올바르지 않습니다."),
    INVALID_VOTE_QUESTION_TYPE(HttpStatus.BAD_REQUEST, "SURVEY-0024", "투표의 질문 타입이 올바르지 않습니다."),
    INVALID_VOTE_FORM_STRUCTURE(HttpStatus.BAD_REQUEST, "SURVEY-0025", "투표의 질문 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
