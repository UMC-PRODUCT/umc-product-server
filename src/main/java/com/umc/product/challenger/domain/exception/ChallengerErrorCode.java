package com.umc.product.challenger.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChallengerErrorCode implements BaseCode {

    CHALLENGER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGER-0001", "사용자를 찾을 수 없습니다."),
    CHALLENGER_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHALLENGER-0002", "이미 등록된 사용자입니다."),
    CHALLENGER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "CHALLENGER-0003", "이미 탈퇴한 사용자입니다."),
    INVALID_CHALLENGER_STATUS(HttpStatus.BAD_REQUEST, "CHALLENGER-0004", "올바르지 않은 사용자 상태입니다."),
    CHALLENGER_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "CHALLENGER-0005", "유효한 챌린저가 아닙니다."),
    INVALID_WORKBOOK_STATUS(HttpStatus.BAD_REQUEST, "CHALLENGER-0006", "워크북 상태가 유효하지 않습니다."),
    CHALLENGER_POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGER-0007", "상벌점 기록을 찾을 수 없습니다."),
    BAD_CHALLENGER_UPDATE_REQUEST(HttpStatus.NOT_FOUND, "CHALLENGER-0008", "잘못된 챌린저 업데이트 요청입니다."),
    NOT_ALLOWED_AUTHOR(HttpStatus.BAD_REQUEST, "CHALLENGER-0009", "활성 또는 수료 상태의 사용자만 일정 생성이 가능합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
