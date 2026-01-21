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
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
