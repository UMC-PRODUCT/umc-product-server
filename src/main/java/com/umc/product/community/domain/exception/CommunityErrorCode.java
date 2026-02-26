package com.umc.product.community.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommunityErrorCode implements BaseCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY-0001", "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY-0002", "댓글을 찾을 수 없습니다."),
    TROPHY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY-0003", "상장을 찾을 수 없습니다."),

    INVALID_POST_TITLE(HttpStatus.BAD_REQUEST, "COMMUNITY-0101", "게시글 제목이 유효하지 않습니다."),
    INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "COMMUNITY-0102", "게시글 내용이 유효하지 않습니다."),
    INVALID_POST_CATEGORY(HttpStatus.BAD_REQUEST, "COMMUNITY-0103", "게시글 카테고리가 유효하지 않습니다."),
    INVALID_POST_REGION(HttpStatus.BAD_REQUEST, "COMMUNITY-0104", "게시글 지역이 유효하지 않습니다."),
    CANNOT_CHANGE_TO_LIGHTNING(HttpStatus.BAD_REQUEST, "COMMUNITY-0105", "일반 게시글을 번개 게시글로 변경할 수 없습니다."),
    CANNOT_CHANGE_FROM_LIGHTNING(HttpStatus.BAD_REQUEST, "COMMUNITY-0106", "번개 게시글을 일반 게시글로 변경할 수 없습니다."),

    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "COMMUNITY-0201", "댓글 내용이 유효하지 않습니다."),
    COMMENT_NOT_OWNED(HttpStatus.FORBIDDEN, "COMMUNITY-0202", "본인의 댓글만 삭제할 수 있습니다."),

    INVALID_TROPHY_WEEK(HttpStatus.BAD_REQUEST, "COMMUNITY-0301", "상장 주차가 유효하지 않습니다."),
    INVALID_TROPHY_TITLE(HttpStatus.BAD_REQUEST, "COMMUNITY-0302", "상장 제목이 유효하지 않습니다."),
    INVALID_TROPHY_CONTENT(HttpStatus.BAD_REQUEST, "COMMUNITY-0303", "상장 내용이 유효하지 않습니다."),
    INVALID_TROPHY_URL(HttpStatus.BAD_REQUEST, "COMMUNITY-0304", "상장 URL이 유효하지 않습니다."),

    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "COMMUNITY-0401", "이미 신고한 게시글/댓글입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
