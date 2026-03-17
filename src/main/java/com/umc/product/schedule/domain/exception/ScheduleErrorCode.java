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
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-0009", "일정을 찾을 수 없습니다"),

    // 상태 오류
    INVALID_ATTENDANCE_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0003", "유효하지 않은 출석 상태입니다"),
    ATTENDANCE_SHEET_INACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0004", "비활성화된 출석부입니다"),
    NOT_ABSENT_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0005", "결석 상태만 인정결석을 신청할 수 있습니다"),

    // 시간 범위
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "SCHEDULE-0006", "시작 시간은 종료 시간보다 이전이어야 합니다"),
    INVALID_LATE_THRESHOLD(HttpStatus.BAD_REQUEST, "SCHEDULE-0007", "지각 인정 시간이 유효하지 않습니다"),
    OUTSIDE_ATTENDANCE_WINDOW(HttpStatus.BAD_REQUEST, "SCHEDULE-0008", "출석 가능한 시간이 아닙니다"),

    // 일정 생성 관련
    TAG_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0010", "태그는 최소 1개 이상 선택해야 합니다"),
    GISU_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0011", "기수 ID는 필수입니다"),

    // 참여자 관련
    PARTICIPANT_NOT_REGISTERED(HttpStatus.FORBIDDEN, "SCHEDULE-0012", "출석 대상자 명단에 등록되지 않았습니다. 관리자에게 문의하세요."),
    CANNOT_UPDATE_STARTED_ATTENDANCE(HttpStatus.BAD_REQUEST, "SCHEDULE-0013",
        "이미 출석이 시작되었습니다. 출석 시작 전에만 명단을 수정할 수 있습니다."),

    // 권한 관련
    NOT_STUDY_GROUP_LEADER(HttpStatus.FORBIDDEN, "SCHEDULE-0014", "스터디 그룹 리더만 일정을 생성할 수 있습니다."),

    // 참여자 명단 수정 관련
    CANNOT_REMOVE_SCHEDULE_AUTHOR(HttpStatus.BAD_REQUEST, "SCHEDULE-0015", "일정 생성자는 참여자 명단에서 제외할 수 없습니다."),

    // 출석 체크 관련
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "SCHEDULE-0015", "이미 출석 체크가 완료되었습니다"),
    CHECK_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0016", "체크 시간은 필수입니다"),

    // 승인/거절 관련
    NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0017", "승인 대기 상태가 아닙니다"),

    // 인정결석 관련
    INVALID_EXCUSE_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0018", "결석 또는 지각 상태에서만 인정결석을 신청할 수 있습니다"),
    EXCUSE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0019", "사유는 필수입니다"),

    // 사유 제출 관련
    INVALID_SUBMIT_REASON_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0020", "출석 전, 지각, 결석 상태에서만 사유를 제출할 수 있습니다"),
    SUBMIT_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0021", "제출 시각은 필수입니다"),

    // 상태 변경 관련
    CANNOT_SET_PENDING_DIRECTLY(HttpStatus.BAD_REQUEST, "SCHEDULE-0022", "PENDING 상태는 직접 변경할 수 없습니다"),
    NOT_CHECKED_IN(HttpStatus.BAD_REQUEST, "SCHEDULE-0023", "출석 체크가 되지 않은 기록입니다"),

    // 출석부 상태 관련
    ATTENDANCE_SHEET_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0024", "이미 비활성화된 출석부입니다"),
    ATTENDANCE_SHEET_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0025", "이미 활성화된 출석부입니다"),

    // 출석 시간대 관련
    START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0026", "시작 시간은 필수입니다"),
    END_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0027", "종료 시간은 필수입니다"),
    BASE_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0028", "기준 시간은 필수입니다"),
    INVALID_BEFORE_MINUTES(HttpStatus.BAD_REQUEST, "SCHEDULE-0029", "이전 시간은 0분 이상이어야 합니다"),
    INVALID_AFTER_MINUTES(HttpStatus.BAD_REQUEST, "SCHEDULE-0030", "이후 시간은 0분 이상이어야 합니다"),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
