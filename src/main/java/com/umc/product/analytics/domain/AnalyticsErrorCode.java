package com.umc.product.analytics.domain;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalyticsErrorCode implements BaseCode {
    RESOURCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ANALYTICS-0001", "운영진 대시보드에 접근할 권한이 없습니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "ANALYTICS-0002", "지원하지 않는 정렬 조건입니다."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "ANALYTICS-0003", "조회 시작 시각은 종료 시각보다 빨라야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
