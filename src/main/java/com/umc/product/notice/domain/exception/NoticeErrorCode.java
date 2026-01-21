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
    NOTICE_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "NOTICE-0008", "공지사항 작성자가 아닙니다."),


    VOTE_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0001", "투표 ID 목록은 필수입니다."),
    IMAGE_URLS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0002", "이미지 URL 목록은 필수입니다."),
    LINK_URLS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0003", "링크 URL 목록은 필수입니다."),
    NOTICE_VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0004", "공지사항 투표를 찾을 수 없습니다."),
    NOTICE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0005", "공지사항 이미지를 찾을 수 없습니다."),
    NOTICE_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0006", "공지사항 링크를 찾을 수 없습니다."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0007", "공지사항 이미지는 최대 10장까지 등록할 수 있습니다."),



    NOT_IMPLEMENTED_YET(HttpStatus.NOT_IMPLEMENTED, "NOTICE-9999", "아직 구현되지 않은 기능입니다.")
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
