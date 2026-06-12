package com.umc.product.schedule.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseCode {

    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "SCHEDULE-0006", "시작 시간은 종료 시간보다 빨라야 해요. 시간을 다시 선택해주세요."),

    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-0009", "일정을 찾을 수 없어요. 선택한 일정을 확인해주세요."),

    TAG_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0010", "태그를 1개 이상 선택해주세요."),

    NOT_FIRST_ATTENDANCE_REQUEST(HttpStatus.BAD_REQUEST, "SCHEDULE-0011", "이미 출석 요청이 있어요. 기존 요청을 확인해주세요."),

    NO_ATTENDANCE_RECORD(HttpStatus.NOT_FOUND, "SCHEDULE-0012", "출석 요청이 없어요. 출석 요청을 먼저 생성해주세요."),

    INVALID_ATTENDANCE_STATUS_FOR_EXCUSE(HttpStatus.BAD_REQUEST, "SCHEDULE-0013",
        "첫 요청, 결석 또는 지각 상태에서만 출석 사유를 제출할 수 있어요. 출석 상태를 확인해주세요."),

    INVALID_ATTENDANCE_STATUS_FOR_APPROVAL(HttpStatus.BAD_REQUEST, "SCHEDULE-0014", "현재 출석 상태에서는 승인할 수 없어요. 출석 상태를 확인해주세요."),

    INVALID_ATTENDANCE_STATUS_FOR_REJECT(HttpStatus.BAD_REQUEST, "SCHEDULE-0015", "현재 출석 상태에서는 거절할 수 없어요. 출석 상태를 확인해주세요."),

    NO_EXCUSE_REASON_GIVEN(HttpStatus.BAD_REQUEST, "SCHEDULE-0016", "출석 인정을 요청하려면 사유를 입력해주세요."),

    ATTENDANCE_NOT_REQUIRES_CONFIRM(HttpStatus.BAD_REQUEST, "SCHEDULE-0017",
        "운영진 확인이 필요한 출석 요청이 아니에요. 출석 상태를 확인해주세요."),

    SCHEDULE_ENDED(HttpStatus.BAD_REQUEST, "SCHEDULE-0018", "종료된 일정에는 출석을 요청할 수 없어요. 일정 시간을 확인해주세요."),

    CHECK_IN_TOO_EARLY(HttpStatus.BAD_REQUEST, "SCHEDULE-0019", "아직 출석할 수 있는 시간이 아니에요. 출석 가능 시간 이후에 다시 시도해주세요."),

    OFFLINE_SCHEDULE_REQUIRES_LOCATION(HttpStatus.BAD_REQUEST, "SCHEDULE-0020", "대면 일정에는 위치 정보가 필요해요. 위치를 입력해주세요."),

    SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST(HttpStatus.BAD_REQUEST, "SCHEDULE-0021", "출석 정책이 없는 일정이에요. 출석 정책을 먼저 설정해주세요."),

    PARTICIPANT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SCHEDULE-0022", "일정 참석자 정보를 찾을 수 없어요. 참석자 목록을 확인해주세요."),

    LOCATION_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SCHEDULE-0023", "출석 인증 범위 안에 있는지 확인하지 못했어요. 위치를 확인한 뒤 다시 시도해주세요."),

    ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION(HttpStatus.BAD_REQUEST, "SCHEDULE-0024",
        "비대면 일정에는 위치 정보를 포함할 수 없어요. 위치 정보를 제거해주세요."),

    NOT_ACTIVE_GISU_SCHEDULE(HttpStatus.BAD_REQUEST, "SCHEDULE-0025",
        "현재 기수의 일정만 만들 수 있어요. 기수를 확인해주세요."),

    NOT_SCHEDULE_PARTICIPANT(HttpStatus.BAD_REQUEST, "SCHEDULE-0026", "일정 참여자만 출석할 수 있어요. 참여자 목록을 확인해주세요."),

    ATTENDANCE_POLICY_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-0027", "출석이 필요한 일정에는 출석 정책을 설정해주세요."),

    STARTED_SCHEDULE_CANT_BE_EDITED(HttpStatus.BAD_REQUEST, "SCHEDULE-0028", "이미 시작된 일정은 수정할 수 없어요. 일정 시간을 확인해주세요."),

    CANNOT_CREATE_SCHEDULE(HttpStatus.FORBIDDEN, "SCHEDULE-0029", "일정을 만들려면 챌린저 활동 이력이 필요해요. 활동 기록을 확인해주세요."),

    EXCEEDED_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "SCHEDULE-0030", "초대 가능한 참여자 수를 초과했어요. 참여자를 줄여주세요."),

    CANNOT_CREATE_ATTENDANCE_REQUIRED_SCHEDULE(HttpStatus.FORBIDDEN, "SCHEDULE-0031",
        "출석이 필요한 일정을 만들 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."),

    INVALID_MEMBER_INVITE(HttpStatus.BAD_REQUEST, "SCHEDULE-0032", "초대할 수 없는 참여자가 포함되어 있어요. 참여자 목록을 확인해주세요."),

    SCHEDULE_HAS_ATTENDANCE_RECORD(HttpStatus.BAD_REQUEST, "SCHEDULE-0033",
        "출석 기록이 있는 일정은 삭제할 수 없어요. 출석 기록을 먼저 확인해주세요."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
