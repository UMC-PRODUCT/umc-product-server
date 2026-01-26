package com.umc.product.recruitment.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecruitmentErrorCode implements BaseCode {

    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-0001", "모집을 찾을 수 없습니다."),
    RECRUITMENT_NOT_DRAFT(HttpStatus.CONFLICT, "RECRUITMENT-0002", "임시저장 상태의 모집만 편집할 수 있습니다."),
    RECRUITMENT_INACTIVE(HttpStatus.CONFLICT, "RECRUITMENT-0003", "활성 중인 모집은 편집할 수 없습니다."),
    RECRUITMENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-004", "해당 타입에 해당하는 모집 일정을 찾을 수 없습니다."),
    RECRUITMENT_ALREADY_PUBLISHED(HttpStatus.BAD_REQUEST, "RECRUITMENT-005", "이미 발행된 모집입니다."),
    RECRUITMENT_PUBLISH_CONFLICT(HttpStatus.BAD_REQUEST, "RECRUITMENT-006", "현재 이미 발행되어 있는 모집이 있습니다."),
    RECRUITMENT_PUBLISH_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "RECRUITMENT-007", "발행하기에 검증되지 않은 필드가 있습니다."),
    RECRUITMENT_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "RECRUITMENT-0008", "발행 상태의 모집만 수정할 수 있습니다."),
    RECRUITMENT_DELETE_FORBIDDEN_HAS_APPLICANTS(HttpStatus.BAD_REQUEST, "RECRUITMENT-0009", "지원자가 있는 모집은 삭제할 수 없습니다."),
    RECRUITMENT_FORM_MISMATCH(HttpStatus.BAD_REQUEST, "RECRUITMENT-00010", "지원하려는 폼이 해당 모집의 폼과 일치하지 않습니다."),
    RECRUITMENT_ALREADY_APPLIED(HttpStatus.CONFLICT, "RECRUITMENT-00011", "이미 지원한 모집입니다."),
    RECRUITMENT_APPLICATION_INCONSISTENT(HttpStatus.INTERNAL_SERVER_ERROR, "RECRUITMENT-00012",
            "지원서 제출 처리 중 내부 데이터 오류가 발생했습니다."),
    PREFERRED_PART_EXCEEDS_MAX_COUNT(HttpStatus.BAD_REQUEST, "RECRUITMENT-00013",
            "선호 파트의 개수가 최대 허용 개수를 초과했습니다."),
    INTERVIEW_TIMETABLE_NOT_SET(HttpStatus.BAD_REQUEST, "RECRUITMENT-00014",
            "면접 일정표가 설정되지 않은 모집입니다."),
    INTERVIEW_PREFERENCE_EMPTY(HttpStatus.BAD_REQUEST, "RECRUITMENT-00015",
            "면접 선호 일시 목록이 비어있습니다."),
    INTERVIEW_PREFERENCE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "RECRUITMENT-00016",
            "면접 선호 일시가 모집의 면접 일정표에 포함되지 않습니다."),
    INTERVIEW_PREFERENCE_INVALID_SLOT(HttpStatus.BAD_REQUEST, "RECRUITMENT-00017",
            "면접 선호 일시가 올바르지 않은 형식입니다."),
    INTERVIEW_PREFERENCE_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "RECRUITMENT-00018",
            "면접 선호 일시의 형식이 올바르지 않습니다."),
    RECRUITMENT_APPLY_WINDOW_NOT_SET(HttpStatus.BAD_REQUEST, "RECRUITMENT-00019",
            "지원 기간이 설정되지 않은 모집입니다."),
    RECRUITMENT_APPLY_WINDOW_INVALID(HttpStatus.BAD_REQUEST, "RECRUITMENT-00020",
            "지원 기간이 올바르지 않습니다."),
    RECRUITMENT_APPLY_NOT_STARTED(HttpStatus.BAD_REQUEST, "RECRUITMENT-00021",
            "아직 지원 기간이 시작되지 않은 모집입니다."),
    RECRUITMENT_APPLY_CLOSED(HttpStatus.BAD_REQUEST, "RECRUITMENT-00022",
            "이미 지원 기간이 종료된 모집입니다."),
    ACTIVE_RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT-0023",
            "활성화된 모집을 찾을 수 없습니다."),
    DRAFT_FORM_RESPONSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "RECRUITMENT-0024",
            "해당 모집에 대한 임시저장된 지원서가 이미 존재합니다."),
    INTERVIEW_TIMETABLE_INVALID(HttpStatus.BAD_REQUEST, "RECRUITMENT-0025",
            "면접 일정표가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
