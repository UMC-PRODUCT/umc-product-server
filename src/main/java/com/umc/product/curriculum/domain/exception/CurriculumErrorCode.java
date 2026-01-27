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
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
