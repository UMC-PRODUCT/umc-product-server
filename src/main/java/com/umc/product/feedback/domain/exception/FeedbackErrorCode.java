package com.umc.product.feedback.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackErrorCode implements BaseCode {

    // UserFeedback
    USER_FEEDBACK_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK-0001", "사용자 피드백 템플릿을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
