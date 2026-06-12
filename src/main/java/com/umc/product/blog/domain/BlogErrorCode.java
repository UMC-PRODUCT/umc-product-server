package com.umc.product.blog.domain;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BlogErrorCode implements BaseCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0001", "콘텐츠를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0002", "댓글을 찾을 수 없습니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "BLOG-0003", "콘텐츠 타입이 유효하지 않습니다."),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "BLOG-0004", "slug가 유효하지 않습니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "BLOG-0005", "ID는 양수여야 합니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "BLOG-0006", "댓글 내용이 유효하지 않습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "BLOG-0007", "닉네임이 유효하지 않습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "BLOG-0008", "대댓글을 달 수 없는 댓글입니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0009", "삭제된 댓글에는 수행할 수 없습니다."),
    INVALID_COMMENT_SORT(HttpStatus.BAD_REQUEST, "BLOG-0011", "댓글 정렬 조건이 유효하지 않습니다."),
    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "BLOG-0012", "회원 ID가 유효하지 않습니다."),
    INVALID_COMMENT_CURSOR(HttpStatus.BAD_REQUEST, "BLOG-0013", "댓글 커서가 유효하지 않습니다."),
    INVALID_CONTENT_TITLE(HttpStatus.BAD_REQUEST, "BLOG-0014", "콘텐츠 제목이 유효하지 않습니다."),
    INVALID_CONTENT_SUMMARY(HttpStatus.BAD_REQUEST, "BLOG-0015", "콘텐츠 요약이 유효하지 않습니다."),
    INVALID_THUMBNAIL_URL(HttpStatus.BAD_REQUEST, "BLOG-0016", "썸네일 URL이 유효하지 않습니다."),
    INVALID_CONTENT_BODY(HttpStatus.BAD_REQUEST, "BLOG-0017", "콘텐츠 본문이 유효하지 않습니다."),
    INVALID_CONTENT_STATUS(HttpStatus.BAD_REQUEST, "BLOG-0018", "콘텐츠 상태가 유효하지 않습니다."),
    CONTENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "BLOG-0019", "이미 존재하는 콘텐츠입니다."),
    CONTENT_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "BLOG-0020", "공개된 콘텐츠에서만 수행할 수 있습니다."),
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0021", "시리즈를 찾을 수 없습니다."),
    SERIES_ALREADY_EXISTS(HttpStatus.CONFLICT, "BLOG-0022", "이미 존재하는 시리즈입니다."),
    INVALID_SERIES_TITLE(HttpStatus.BAD_REQUEST, "BLOG-0023", "시리즈 제목이 유효하지 않습니다."),
    INVALID_SERIES_DESCRIPTION(HttpStatus.BAD_REQUEST, "BLOG-0024", "시리즈 설명이 유효하지 않습니다."),
    INVALID_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "BLOG-0025", "표시 순서가 유효하지 않습니다."),
    CONTENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "BLOG-0026", "시리즈와 콘텐츠 타입이 일치하지 않습니다."),
    HASHTAG_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0027", "해시태그를 찾을 수 없습니다."),
    INVALID_HASHTAG(HttpStatus.BAD_REQUEST, "BLOG-0028", "해시태그가 유효하지 않습니다."),
    TOO_MANY_HASHTAGS(HttpStatus.BAD_REQUEST, "BLOG-0029", "해시태그는 최대 10개까지 등록할 수 있습니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "BLOG-0030", "정렬 조건이 유효하지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "BLOG-0031", "커서가 유효하지 않습니다."),
    CONTENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0032", "삭제된 콘텐츠입니다."),
    SERIES_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0033", "삭제된 시리즈입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
