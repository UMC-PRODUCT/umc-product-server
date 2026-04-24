package com.umc.product.schedule.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseCode {

    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "SCHEDULE-0006", "시작 시간은 종료 시간보다 이전이어야 합니다"),

    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-0009", "일정을 찾을 수 없습니다"),

    TAG_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0010", "태그는 최소 1개 이상 선택해야 합니다"),

    NOT_FIRST_ATTENDANCE_REQUEST(HttpStatus.BAD_REQUEST, "SCHEDULE-0011", "기존 출석 요청이 존재합니다."),

    NO_ATTENDANCE_RECORD(HttpStatus.NOT_FOUND, "SCHEDULE-0012", "출석 요청이 존재하지 않습니다. 출석 요청을 생성하고 다시 시도해주세요."),

    INVALID_ATTENDANCE_STATUS_FOR_EXCUSE(HttpStatus.BAD_REQUEST, "SCHEDULE-0013",
        "출석 사유 제출은 첫 요청, 결석 또는 지각 상태에서만 가능합니다."),

    INVALID_ATTENDANCE_STATUS_FOR_APPROVAL(HttpStatus.BAD_REQUEST, "SCHEDULE-0014", "현재 출석 상태에서는 승인이 불가능합니다."),

    INVALID_ATTENDANCE_STATUS_FOR_REJECT(HttpStatus.BAD_REQUEST, "SCHEDULE-0015", "출석 요청에 대한 거절을 할 수 없는 상태입니다."),

    NO_EXCUSE_REASON_GIVEN(HttpStatus.BAD_REQUEST, "SCHEDULE-0016", "출석 인정을 요청하는 사유가 제공되지 않았거나 비어있습니다."),

    ATTENDANCE_NOT_REQUIRES_CONFIRM(HttpStatus.BAD_REQUEST, "SCHEDULE-0017",
        "해당 출석 요청은 운영진의 승인 또는 기각을 필요로 하는 상태가 아닙니다."),

    SCHEDULE_ENDED(HttpStatus.BAD_REQUEST, "SCHEDULE-0018", "종료된 일정에 대한 출석 요청은 허용되지 않습니다."),

    CHECK_IN_TOO_EARLY(HttpStatus.BAD_REQUEST, "SCHEDULE-0019", "출석 가능한 시간 이전입니다. 출석 가능한 시간 이후에 다시 시도해주세요."),

    OFFLINE_SCHEDULE_REQUIRES_LOCATION(HttpStatus.BAD_REQUEST, "SCHEDULE-0020", "대면 일정은 위치 정보가 필수입니다."),

    SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST(HttpStatus.BAD_REQUEST, "SCHEDULE-0021", "출석 정책이 존재하지 않아 출석 요청이 불가능한 일정입니다."),

    PARTICIPANT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SCHEDULE-0022", "일정에 대한 참석자 정보가 존재하지 않습니다."),

    LOCATION_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SCHEDULE-0023", "사용자가 출석 인증 범위에 있지 않습니다."),

    ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION(HttpStatus.BAD_REQUEST, "SCHEDULE-0024",
        "비대면 일정으로 변경 시 위치 정보를 포함할 수 없습니다."),

    NOT_ACTIVE_GISU_SCHEDULE(HttpStatus.BAD_REQUEST, "SCHEDULE-0025",
        "현재 기수의 일정만 생성할 수 있습니다."),

    NOT_SCHEDULE_PARTICIPANT(HttpStatus.BAD_REQUEST, "SCHEDULE-0026", "일정의 참여자가 아닙니다."),

    ATTENDANCE_POLICY_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0027", "출석을 요하는 일정의 출석 정책은 필수입니다."),

    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
