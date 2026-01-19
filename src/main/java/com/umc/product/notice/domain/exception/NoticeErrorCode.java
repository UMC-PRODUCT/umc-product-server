package com.umc.product.notice.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NoticeErrorCode implements BaseCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-0001", "공지사항을 찾을 수 없습니다."),
    ALREADY_PUBLISHED_NOTICE(HttpStatus.BAD_REQUEST, "NOTICE-0002", "이미 게시된 공지사항입니다."),
    INVALID_NOTICE_TITLE(HttpStatus.BAD_REQUEST, "NOTICE-0003", "공지사항 제목이 유효하지 않습니다."),
    INVALID_NOTICE_CONTENT(HttpStatus.BAD_REQUEST, "NOTICE-0004", "공지사항 내용이 유효하지 않습니다."),
    INVALID_NOTICE_STATUS_FOR_REMINDER(HttpStatus.BAD_REQUEST, "NOTICE-0005", "공지사항 알림을 보낼 수 없는 상태입니다."),
    AUTHOR_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-0006", "공지사항 작성자는 필수입니다."),
    NOTICE_SCOPE_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-0007", "공지사항 대상 범위는 필수입니다."),


    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
