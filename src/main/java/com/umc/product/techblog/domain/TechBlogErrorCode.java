package com.umc.product.techblog.domain;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TechBlogErrorCode implements BaseCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TECH-BLOG-0001", "콘텐츠를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TECH-BLOG-0002", "댓글을 찾을 수 없습니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "TECH-BLOG-0003", "콘텐츠 타입이 유효하지 않습니다."),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "TECH-BLOG-0004", "slug가 유효하지 않습니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "TECH-BLOG-0005", "ID는 양수여야 합니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "TECH-BLOG-0006", "댓글 내용이 유효하지 않습니다."),
    INVALID_GUEST_NICKNAME(HttpStatus.BAD_REQUEST, "TECH-BLOG-0007", "비회원 닉네임이 유효하지 않습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "TECH-BLOG-0008", "대댓글을 달 수 없는 댓글입니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "TECH-BLOG-0009", "삭제된 댓글에는 수행할 수 없습니다."),
    INVALID_COMMENT_SORT(HttpStatus.BAD_REQUEST, "TECH-BLOG-0011", "댓글 정렬 조건이 유효하지 않습니다."),
    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "TECH-BLOG-0012", "회원 ID가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
