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
    ALREADY_COMPLETED_PROJECT(HttpStatus.BAD_REQUEST, "PROJECT-0001", "이미 완료된 프로젝트입니다."),
    PROJECT_ABORT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0001", "해당 프로젝트를 해산시킬 수 없습니다."),

    // ProjectApplication
    APPLICATION_APPROVE_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0001", "해당 지원서를 합격시킬 수 없습니다."),
    APPLICATION_REJECT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0001", "해당 지원서를 불합격시킬 수 없습니다."),
    APPLICATION_CANCEL_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0001", "해당 지원서를 철회할 수 없습니다."),
    APPLICATION_SUBMIT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0001", "해당 지원서를 최종 제출할 수 없습니다."),

    // ProjectApplicationForm
    APPLICATION_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0001", "프로젝트에서 해당 지원용 폼을 찾을 수 없습니다."),
    APPLICATION_FORM_ACCESS_NOT_ALLOWED(HttpStatus.FORBIDDEN, "PROJECT-0001", "요청하신 지원용 폼 섹션에 접근 권한이 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
