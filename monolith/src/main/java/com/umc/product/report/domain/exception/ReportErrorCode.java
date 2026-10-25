package com.umc.product.report.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 신고 도메인 에러 코드.
 * <p>
 * 기존 클라이언트 호환을 위해 community에서 옮겨 온 코드 값(COMMUNITY-0016, COMMUNITY-0046)을 그대로 유지합니다.
 */
@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseCode {

    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "COMMUNITY-0016", "이미 신고한 게시글, 댓글 또는 스레드 메시지예요. 신고 내역을 확인해주세요."),
    REPORT_SELF_TARGET(HttpStatus.FORBIDDEN, "COMMUNITY-0046", "내가 작성한 메시지는 신고할 수 없어요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
