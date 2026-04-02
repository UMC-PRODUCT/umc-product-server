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
    WORKBOOK_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "CURRICULUM-0008", "이미 제출된 워크북은 다시 제출할 수 없습니다."),
    WORKBOOK_NOT_REVIEWABLE(HttpStatus.BAD_REQUEST, "CURRICULUM-0013", "제출된 상태의 워크북만 심사할 수 있습니다."),
    WORKBOOK_NOT_SELECTABLE_FOR_BEST(HttpStatus.BAD_REQUEST, "CURRICULUM-0014", "통과 상태의 워크북만 베스트로 선정할 수 있습니다."),
    WORKBOOK_SUBMISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0009", "이미 해당 주차의 워크북 미션을 제출하였습니다."),
    CURRICULUM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0010", "해당 기수와 파트의 커리큘럼이 이미 존재합니다."),
    WORKBOOK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CURRICULUM-0011", "해당 워크북에 대한 접근 권한이 없습니다."),
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0012", "제출 정보를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "CURRICULUM-0015", "이미 해당 워크북에 대한 리뷰를 작성하였습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "CURRICULUM-0016", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_BEST(HttpStatus.CONFLICT, "CURRICULUM-0017", "이미 베스트로 선정된 리뷰입니다."),
    REVIEW_NOT_BEST(HttpStatus.BAD_REQUEST, "CURRICULUM-0018", "베스트 상태의 리뷰만 취소할 수 있습니다."),
    WORKBOOK_NOT_BEST(HttpStatus.BAD_REQUEST, "CURRICULUM-0019", "베스트 상태의 워크북만 취소할 수 있습니다."),
    REVIEW_IS_BEST(HttpStatus.BAD_REQUEST, "CURRICULUM-0020", "베스트 상태의 리뷰는 수정할 수 없습니다. 베스트 취소 후 수정해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
