package com.umc.product.project.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements BaseCode {

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0001", "프로젝트를 찾을 수 없습니다."),
    ALREADY_COMPLETED_PROJECT(HttpStatus.BAD_REQUEST, "PROJECT-0002", "이미 완료된 프로젝트입니다."),
    PROJECT_ABORT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0003", "해당 프로젝트를 해산시킬 수 없습니다."),

    // ProjectApplication
    APPLICATION_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "PROJECT-0004", "요청하신 조작은 지원서가 제출된 상태에서만 가능합니다."),
    APPLICATION_SUBMIT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PROJECT-0005", "이미 지원서가 제출되었거나 평가가 완료된 상태입니다."),

    // ProjectApplicationForm
    APPLICATION_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0006", "프로젝트에서 해당 지원용 폼을 찾을 수 없습니다."),
    APPLICATION_FORM_ACCESS_NOT_ALLOWED(HttpStatus.FORBIDDEN, "PROJECT-0007", "요청하신 지원용 폼 섹션에 접근 권한이 없습니다."),
    APPLICATION_FORM_POLICY_PARTS_EMPTY(HttpStatus.BAD_REQUEST, "PROJECT-0013", "PART 타입 섹션은 1개 이상의 파트를 지정해야 합니다."),
    APPLICATION_FORM_INVALID_SECTION_ID(HttpStatus.BAD_REQUEST, "PROJECT-0014", "현재 폼에 존재하지 않는 sectionId 입니다."),
    APPLICATION_FORM_INVALID_QUESTION_ID(HttpStatus.BAD_REQUEST, "PROJECT-0015", "해당 섹션에 속하지 않는 questionId 입니다."),
    APPLICATION_FORM_INVALID_OPTION_ID(HttpStatus.BAD_REQUEST, "PROJECT-0016", "해당 질문에 속하지 않는 optionId 입니다."),
    APPLICATION_FORM_OPTIONS_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PROJECT-0017", "선택지 타입(RADIO/CHECKBOX/DROPDOWN)이 아닌 질문에는 옵션을 지정할 수 없습니다."),
    APPLICATION_FORM_OPTIONS_REQUIRED(HttpStatus.BAD_REQUEST, "PROJECT-0018", "선택지 타입 질문에는 1개 이상의 옵션이 필요합니다."),

    // Project Draft flow (PROJECT-101, 102, 107)
    PROJECT_DUPLICATE_IN_GISU(HttpStatus.CONFLICT, "PROJECT-0008", "이미 해당 기수에 등록한 프로젝트가 있습니다."),
    PROJECT_INVALID_STATE(HttpStatus.BAD_REQUEST, "PROJECT-0009", "현재 상태에서 수행할 수 없는 작업입니다."),
    PROJECT_OWNER_NOT_PLAN_CHALLENGER(HttpStatus.BAD_REQUEST, "PROJECT-0010", "프로젝트 PO는 PLAN 파트 챌린저여야 합니다."),
    PROJECT_SUBMIT_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "PROJECT-0011", "제출에 필요한 필수 정보가 누락되었습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT-0012", "해당 프로젝트에 대한 접근 권한이 없습니다."),

    // ProjectMember (PROJECT-003/004/005)
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0100", "프로젝트 멤버를 찾을 수 없습니다."),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROJECT-0101", "이미 해당 프로젝트의 멤버입니다."),
    PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER(HttpStatus.BAD_REQUEST, "PROJECT-0102", "메인 PM 은 팀원 제거가 아닌 소유권 양도 API 로 변경해야 합니다."),

    // ProjectPartQuota / Publish (PROJECT-105/108)
    PROJECT_PART_QUOTA_INVALID(HttpStatus.BAD_REQUEST, "PROJECT-0200", "파트 정원은 1 이상이어야 합니다."),
    PROJECT_PART_QUOTA_REQUIRED(HttpStatus.BAD_REQUEST, "PROJECT-0202", "공개하려면 파트별 정원이 1개 이상 등록되어 있어야 합니다."),
    PROJECT_PART_QUOTA_DUPLICATE(HttpStatus.BAD_REQUEST, "PROJECT-0203", "동일 파트가 중복으로 입력되었습니다."),

    // ProjectMatchingRound
    PROJECT_MATCHING_ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0300", "매칭 차수를 찾을 수 없습니다."),
    PROJECT_MATCHING_ROUND_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "PROJECT-0301",
        "매칭 차수 기간은 startsAt < endsAt < decisionDeadline 순서여야 합니다."),
    PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED(HttpStatus.CONFLICT, "PROJECT-0302",
        "같은 지부 내에서는 매칭 차수 기간이 중복될 수 없습니다."),
    PROJECT_MATCHING_ROUND_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT-0303",
        "해당 매칭 차수에 대한 관리 권한이 없습니다."),
    PROJECT_MATCHING_ROUND_DELETE_CONFLICT(HttpStatus.CONFLICT, "PROJECT-0304",
        "연관된 지원서가 있는 매칭 차수는 삭제할 수 없습니다."),
    PROJECT_MATCHING_ROUND_TIME_REQUIRES_CHAPTER(HttpStatus.BAD_REQUEST, "PROJECT-0305",
        "time 기준 조회는 chapterId와 함께 요청해야 합니다."),

    // ProjectApplication (APPLY-001/002/003)
    PROJECT_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-0204", "작성 중인 지원서를 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
