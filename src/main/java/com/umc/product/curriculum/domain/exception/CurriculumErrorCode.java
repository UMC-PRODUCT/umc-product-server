package com.umc.product.curriculum.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CurriculumErrorCode implements BaseCode {

    CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0001", "커리큘럼을 찾을 수 없습니다."),
    WORKBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0002", "워크북을 찾을 수 없습니다."),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0003", "미션을 찾을 수 없습니다."),
    WORKBOOK_HAS_SUBMISSIONS(HttpStatus.CONFLICT, "CURRICULUM-0004", "제출된 워크북이 있어 삭제할 수 없습니다."),
    WORKBOOK_NOT_IN_CURRICULUM(HttpStatus.NOT_FOUND, "CURRICULUM-0005", "커리큘럼에 해당 워크북이 존재하지 않습니다."),
    CHALLENGER_WORKBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0006", "챌린저 워크북을 찾을 수 없습니다."),
    SUBMISSION_REQUIRED(HttpStatus.BAD_REQUEST, "CURRICULUM-0007", "제출 내용이 필요합니다."),
    INVALID_WORKBOOK_STATUS(HttpStatus.BAD_REQUEST, "CURRICULUM-0008", "워크북 상태가 유효하지 않습니다."),
    WORKBOOK_SUBMISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0009", "이미 해당 주차의 워크북 미션을 제출하였습니다."),
    CURRICULUM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0010", "해당 기수와 파트의 커리큘럼이 이미 존재합니다."),
    WORKBOOK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CURRICULUM-0011", "해당 워크북에 대한 접근 권한이 없습니다."),
    INVALID_WEEKLY_CURRICULUM_PERIOD(HttpStatus.BAD_REQUEST, "CURRICULUM-0012", "주차 커리큘럼의 시작일이 종료일보다 늦을 수 없습니다."),
    INVALID_WORKBOOK_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "CURRICULUM-0013", "유효하지 않은 워크북 상태 변경 요청입니다."),
    WEEKLY_CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0014", "주차별 커리큘럼을 찾을 수 없습니다."),
    CURRICULUM_HAS_WEEKLY_CURRICULUMS(HttpStatus.CONFLICT, "CURRICULUM-0015", "주차별 커리큘럼이 존재하여 커리큘럼을 삭제할 수 없습니다."),
    WEEKLY_CURRICULUM_HAS_WORKBOOKS(HttpStatus.CONFLICT, "CURRICULUM-0016", "원본 워크북이 존재하여 주차별 커리큘럼을 삭제할 수 없습니다."),
    WEEKLY_CURRICULUM_DATE_LOCKED(HttpStatus.CONFLICT, "CURRICULUM-0017", "배포된 워크북이 존재하여 주차 기간을 수정할 수 없습니다."),
    WEEKLY_CURRICULUM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0018", "이미 동일한 주차와 부록 여부를 가진 주차별 커리큘럼이 존재합니다."),
    WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "CURRICULUM-0019",
        "이미 종료된 기간으로 주차별 커리큘럼을 생성하거나 수정할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
