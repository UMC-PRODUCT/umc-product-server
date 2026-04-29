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
    APPLICATION_FORM_POLICY_PARTS_EMPTY(HttpStatus.BAD_REQUEST, "PROJECT-0013", "PART 타입 섹션은 1개 이상의 파트를 지정해야 합니다."),

    // Project Draft flow (PROJECT-101, 102, 107)
    PROJECT_DUPLICATE_IN_GISU(HttpStatus.CONFLICT, "PROJECT-0008", "이미 해당 기수에 등록한 프로젝트가 있습니다."),
    PROJECT_INVALID_STATE(HttpStatus.BAD_REQUEST, "PROJECT-0009", "현재 상태에서 수행할 수 없는 작업입니다."),
    PROJECT_OWNER_NOT_PLAN_CHALLENGER(HttpStatus.BAD_REQUEST, "PROJECT-0010", "프로젝트 PO는 PLAN 파트 챌린저여야 합니다."),
    PROJECT_SUBMIT_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "PROJECT-0011", "제출에 필요한 필수 정보가 누락되었습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT-0012", "해당 프로젝트에 대한 접근 권한이 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
