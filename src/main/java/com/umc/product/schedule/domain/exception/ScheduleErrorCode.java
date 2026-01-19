package com.umc.product.schedule.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseCode {

    // 조회 실패
    ATTENDANCE_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-0001", "출석부를 찾을 수 없습니다"),
    ATTENDANCE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-0002", "출석 기록을 찾을 수 없습니다"),

    // 상태 오류
    INVALID_ATTENDANCE_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0003", "유효하지 않은 출석 상태입니다"),
    ATTENDANCE_SHEET_INACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0004", "비활성화된 출석부입니다"),
    NOT_ABSENT_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0005", "결석 상태만 인정결석을 신청할 수 있습니다"),

    // 시간 범위
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "SCHEDULE-0006", "시작 시간은 종료 시간보다 이전이어야 합니다"),
    INVALID_LATE_THRESHOLD(HttpStatus.BAD_REQUEST, "SCHEDULE-0007", "지각 인정 시간이 유효하지 않습니다"),

    // 일정 생성 관련
    NOT_ACTIVE_CHALLENGER(HttpStatus.FORBIDDEN, "SCHEDULE-0008", "현재 활성 기수의 챌린저만 일정을 생성할 수 있습니다."),
    ;
    //별도 추가 예정
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
