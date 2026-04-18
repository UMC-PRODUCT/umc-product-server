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
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "SCHEDULE-0016", "이미 출석 체크가 완료되었습니다"),
    CHECK_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0017", "체크 시간은 필수입니다"),

    // 승인/거절 관련
    NOT_PENDING_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0018", "승인 대기 상태가 아닙니다"),

    // 인정결석 관련
    INVALID_EXCUSE_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0019", "결석 또는 지각 상태에서만 인정결석을 신청할 수 있습니다"),
    EXCUSE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0020", "사유는 필수입니다"),

    // 사유 제출 관련
    INVALID_SUBMIT_REASON_STATUS(HttpStatus.BAD_REQUEST, "SCHEDULE-0021", "출석 전, 지각, 결석 상태에서만 사유를 제출할 수 있습니다"),
    SUBMIT_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0022", "제출 시각은 필수입니다"),

    // 상태 변경 관련
    CANNOT_SET_PENDING_DIRECTLY(HttpStatus.BAD_REQUEST, "SCHEDULE-0023", "PENDING 상태는 직접 변경할 수 없습니다"),
    NOT_CHECKED_IN(HttpStatus.BAD_REQUEST, "SCHEDULE-0024", "출석 체크가 되지 않은 기록입니다"),

    // 출석부 상태 관련
    ATTENDANCE_SHEET_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0025", "이미 비활성화된 출석부입니다"),
    ATTENDANCE_SHEET_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "SCHEDULE-0026", "이미 활성화된 출석부입니다"),

    // 출석 시간대 관련
    START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0027", "시작 시간은 필수입니다"),
    END_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0028", "종료 시간은 필수입니다"),
    BASE_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0029", "기준 시간은 필수입니다"),
    INVALID_BEFORE_MINUTES(HttpStatus.BAD_REQUEST, "SCHEDULE-0030", "이전 시간은 0분 이상이어야 합니다"),
    INVALID_AFTER_MINUTES(HttpStatus.BAD_REQUEST, "SCHEDULE-0031", "이후 시간은 0분 이상이어야 합니다"),

    NOT_FIRST_ATTENDANCE_REQUEST(HttpStatus.BAD_REQUEST, "SCHEDULE-0032", "기존 출석 요청이 존재합니다."),
    NO_ATTENDANCE_RECORD(HttpStatus.NOT_FOUND, "SCHEDULE-0033", "출석 요청이 존재하지 않습니다. 출석 요청을 생성하고 다시 시도해주세요."),
    INVALID_ATTENDANCE_STATUS_FOR_EXCUSE(HttpStatus.BAD_REQUEST, "SCHEDULE-0034", "출석 인정은 지각 또는 결석 상태에서만 가능합니다."),
    INVALID_ATTENDANCE_STATUS_FOR_APPROVAL(HttpStatus.BAD_REQUEST, "SCHEDULE-0035", "현재 출석 상태에서는 승인이 불가능합니다."),
    INVALID_ATTENDANCE_STATUS_FOR_REJECT(HttpStatus.BAD_REQUEST, "SCHEDULE-0036", "출석 요청에 대한 거절을 할 수 없는 상태입니다."),
    NO_EXCUSE_REASON_GIVEN(HttpStatus.BAD_REQUEST, "SCHEDULE-0037", "출석 인정을 요청하는 사유가 제공되지 않았거나 비어있습니다."),
    ATTENDANCE_NOT_REQUIRES_CONFIRM(HttpStatus.BAD_REQUEST, "SCHEDULE-0038",
        "해당 출석 요청은 운영진의 승인 또는 기각을 필요로 하는 상태가 아닙니다."),

    SCHEDULE_ENDED(HttpStatus.BAD_REQUEST, "SCHEDULE-0039", "종료된 일정에 대한 출석 요청은 허용되지 않습니다."),
    NO_SCHEDULE_POLICY(HttpStatus.NOT_FOUND, "SCHEDULE-0040", "출결 정책이 존재하지 않습니다."),
    CHECK_IN_TOO_EARLY(HttpStatus.BAD_REQUEST, "SCHEDULE-0041", "출석 가능한 시간 이전입니다. 출석 가능한 시간 이후에 다시 시도해주세요."),
    INVALID_ATTENDANCE_POLICY(HttpStatus.BAD_REQUEST, "SCHEDULE-0042", "출석 정책 시간이 유효하지 않습니다."),

    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0043", "위치 정보 입력은 필수입니다."),
    OFFLINE_SCHEDULE_REQUIRES_LOCATION(HttpStatus.BAD_REQUEST, "SCHEDULE-0044", "대면 일정은 위치 정보가 필수입니다."),
    ONLINE_SCHEDULE_CANNOT_HAVE_POLICY(HttpStatus.BAD_REQUEST, "SCHEDULE-0045", "비대면 일정은 출석 정책을 가질 수 없습니다."),
    ONLINE_SCHEDULE_ATTENDANCE_REQUEST_IMPOSSIBLE(HttpStatus.BAD_REQUEST, "SCHEDULE-0046", "비대면 일정은 출석 요청을 할 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SCHEDULE-0047", "일정에 대한 참석자 정보가 존재하지 않습니다."),
    LOCATION_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SCHEDULE-0048", "사용자가 출석 인증 범위에 있지 않습니다."),

    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
