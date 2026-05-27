package com.umc.product.maintenance.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MaintenanceErrorCode implements BaseCode {

    SERVICE_UNDER_MAINTENANCE(HttpStatus.SERVICE_UNAVAILABLE, "MAINTENANCE-0001", "서비스 점검 중입니다."),
    MAINTENANCE_WINDOW_NOT_FOUND(HttpStatus.NOT_FOUND, "MAINTENANCE-0002", "점검 윈도우를 찾을 수 없습니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "MAINTENANCE-0003", "종료 시각은 시작 시각 이후여야 합니다."),
    START_AT_IN_PAST(HttpStatus.BAD_REQUEST, "MAINTENANCE-0004", "시작 시각은 현재 시각 이후여야 합니다."),
    TARGET_DOMAINS_REQUIRED(HttpStatus.BAD_REQUEST, "MAINTENANCE-0005", "PER_DOMAIN 점검은 대상 도메인을 1개 이상 지정해야 합니다."),
    OVERLAPPING_WINDOW(HttpStatus.CONFLICT, "MAINTENANCE-0006", "다른 점검 윈도우와 시간이 겹칩니다."),
    ALREADY_ENDED(HttpStatus.BAD_REQUEST, "MAINTENANCE-0007", "이미 종료된 점검 윈도우입니다."),
    NOT_SUPER_ADMIN(HttpStatus.FORBIDDEN, "MAINTENANCE-0008", "점검 관리 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
