package com.umc.product.recruitment.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {

    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-0001", "모집을 찾을 수 없습니다."),
    RECRUITMENT_NOT_DRAFT(HttpStatus.CONFLICT, "RECRUITMENT-0002", "임시저장 상태의 모집만 편집할 수 있습니다."),
    RECRUITMENT_INACTIVE(HttpStatus.CONFLICT, "RECRUITMENT-0003", "활성 중인 모집은 편집할 수 없습니다."),
    RECRUITMENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-004", "해당 타입에 해당하는 모집 일정을 찾을 수 없습니다."),
    RECRUITMENT_ALREADY_PUBLISHED(HttpStatus.BAD_REQUEST, "RECRUITMENT-005", "이미 발행된 모집입니다."),
    RECRUITMENT_PUBLISH_CONFLICT(HttpStatus.BAD_REQUEST, "RECRUITMENT-006", "현재 이미 발행되어 있는 모집이 있습니다."),
    RECRUITMENT_PUBLISH_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "RECRUITMENT-007", "발행하기에 검증되지 않은 필드가 있습니다."),
    RECRUITMENT_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "RECRUITMENT-0008", "발행 상태의 모집만 수정할 수 있습니다."),
    RECRUITMENT_DELETE_FORBIDDEN_HAS_APPLICANTS(HttpStatus.BAD_REQUEST, "RECRUITMENT-0009", "지원자가 있는 모집은 삭제할 수 없습니다."),
    RECRUITMENT_FORM_MISMATCH(HttpStatus.BAD_REQUEST, "RECRUITMENT-00010", "지원하려는 폼이 해당 모집의 폼과 일치하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
