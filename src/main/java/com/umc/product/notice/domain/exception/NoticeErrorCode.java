package com.umc.product.notice.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NoticeErrorCode implements BaseCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-0001", "공지를 찾을 수 없어요. 목록을 새로고침해주세요."),
    ALREADY_PUBLISHED_NOTICE(HttpStatus.BAD_REQUEST, "NOTICE-0002", "이미 게시된 공지예요. 게시 상태를 확인해주세요."),
    INVALID_NOTICE_TITLE(HttpStatus.BAD_REQUEST, "NOTICE-0003", "공지 제목이 올바르지 않아요. 제목을 확인해주세요."),
    INVALID_NOTICE_CONTENT(HttpStatus.BAD_REQUEST, "NOTICE-0004", "공지 내용이 올바르지 않아요. 내용을 확인해주세요."),
    INVALID_NOTICE_STATUS_FOR_REMINDER(HttpStatus.BAD_REQUEST, "NOTICE-0005", "현재 상태에서는 공지 알림을 보낼 수 없어요. 공지 상태를 확인해주세요."),
    AUTHOR_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-0006", "공지 작성자 정보가 필요해요. 로그인 정보를 확인해주세요."),
    NOTICE_SCOPE_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-0007", "공지 대상 범위를 선택해주세요."),
    NOTICE_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "NOTICE-0008", "공지 작성자만 수정할 수 있어요."),
    NO_WRITE_PERMISSION(HttpStatus.FORBIDDEN, "NOTICE-0009",
        "공지를 작성할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."),
    NO_READ_PERMISSION(HttpStatus.FORBIDDEN, "NOTICE-0012",
        "공지를 조회할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."),
    INVALID_TARGET_SETTING(HttpStatus.BAD_REQUEST, "NOTICE-0010", "공지 수신자 설정이 올바르지 않아요. 대상 설정을 확인해주세요."),
    NO_TARGET_FOUND(HttpStatus.NOT_FOUND, "NOTICE-0011", "공지 수신 대상을 찾을 수 없어요. 대상 설정을 다시 확인해주세요."),


    VOTE_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0001", "투표를 1개 이상 선택해주세요."),
    IMAGE_URLS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0002", "이미지 링크를 1개 이상 입력해주세요."),
    LINK_URLS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0003", "공지 링크를 1개 이상 입력해주세요."),
    NOTICE_VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0004", "공지 투표를 찾을 수 없어요. 투표를 다시 선택해주세요."),
    NOTICE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0005", "공지 이미지를 찾을 수 없어요. 이미지를 다시 선택해주세요."),
    NOTICE_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-CONTENTS-0006", "공지 링크를 찾을 수 없어요. 링크를 다시 선택해주세요."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0007", "공지 이미지는 최대 10장까지 등록할 수 있어요."),
    VOTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "NOTICE-CONTENTS-0008", "이 공지에는 이미 투표가 있어요. 기존 투표를 확인해주세요."),
    INVALID_VOTE_OPTION_COUNT(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0009", "투표 선택지는 2개 이상 5개 이하로 입력해주세요."),
    INVALID_VOTE_OPTION_CONTENT(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0010", "투표 선택지에 빈 값이 있어요. 선택지 내용을 확인해주세요."),
    VOTE_NOT_STARTED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0011", "아직 투표 기간이 시작되지 않았어요. 시작 후 다시 시도해주세요."),
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0012", "이미 종료된 투표예요. 투표 기간을 확인해주세요."),
    SELECTED_OPTION_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTICE-CONTENTS-0013", "투표 선택지를 1개 이상 선택해주세요."),


    NOT_IMPLEMENTED_YET(HttpStatus.NOT_IMPLEMENTED, "NOTICE-9999", "아직 사용할 수 없는 기능이에요. 필요한 기능이라면 서버팀에 문의해주세요.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
