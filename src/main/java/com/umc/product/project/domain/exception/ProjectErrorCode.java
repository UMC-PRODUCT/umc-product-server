package com.umc.product.project.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements BaseCode {

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0001", "프로젝트를 찾을 수 없습니다."),
    ALREADY_COMPLETED_PROJECT(HttpStatus.BAD_REQUEST, "PROJECT-0002", "이미 완료된 프로젝트입니다."),
    PROJECT_ABORT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0003", "해당 프로젝트를 해산시킬 수 없습니다."),

    // ProjectApplication
    APPLICATION_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "PROJECT-0004", "요청하신 조작은 지원서가 제출된 상태에서만 가능합니다."),
    APPLICATION_SUBMIT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0005", "이미 지원서가 제출되었거나 평가가 완료된 상태입니다."),

    // ProjectApplicationForm
    APPLICATION_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0006", "프로젝트에서 해당 지원용 폼을 찾을 수 없습니다."),
    APPLICATION_FORM_ACCESS_NOT_ALLOWED(HttpStatus.FORBIDDEN, "PROJECT-0007", "요청하신 지원용 폼 섹션에 접근 권한이 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
