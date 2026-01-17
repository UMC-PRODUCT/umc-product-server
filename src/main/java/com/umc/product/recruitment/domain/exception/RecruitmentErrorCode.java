package com.umc.product.recruitment.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {

    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-0001", "모집을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
