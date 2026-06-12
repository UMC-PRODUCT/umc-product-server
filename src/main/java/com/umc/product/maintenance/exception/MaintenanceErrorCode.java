package com.umc.product.maintenance.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MaintenanceErrorCode implements BaseCode {

    SERVICE_UNDER_MAINTENANCE(HttpStatus.SERVICE_UNAVAILABLE, "MAINTENANCE-0001", "서비스 점검 중이에요. 점검이 끝난 뒤 다시 시도해주세요."),
    MAINTENANCE_WINDOW_NOT_FOUND(HttpStatus.NOT_FOUND, "MAINTENANCE-0002", "점검 일정을 찾을 수 없어요. 선택한 일정을 확인해주세요."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "MAINTENANCE-0003", "종료 시각은 시작 시각 이후로 선택해주세요."),
    START_AT_IN_PAST(HttpStatus.BAD_REQUEST, "MAINTENANCE-0004", "시작 시각은 현재 시각 이후로 선택해주세요."),
    TARGET_DOMAINS_REQUIRED(HttpStatus.BAD_REQUEST, "MAINTENANCE-0005", "도메인별 점검은 대상 도메인을 1개 이상 선택해주세요."),
    OVERLAPPING_WINDOW(HttpStatus.CONFLICT, "MAINTENANCE-0006", "다른 점검 일정과 시간이 겹쳐요. 시간을 다시 선택해주세요."),
    ALREADY_ENDED(HttpStatus.BAD_REQUEST, "MAINTENANCE-0007", "이미 종료된 점검 일정이에요. 진행 중이거나 예정된 일정을 선택해주세요."),
    NOT_SUPER_ADMIN(HttpStatus.FORBIDDEN, "MAINTENANCE-0008",
        "점검을 관리할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
