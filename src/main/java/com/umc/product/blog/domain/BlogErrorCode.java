package com.umc.product.blog.domain;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BlogErrorCode implements BaseCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0001", "글을 찾지 못했어요. 주소를 확인해주세요."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0002", "댓글을 찾지 못했어요. 새로고침 후 다시 시도해주세요."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "BLOG-0003", "카테고리를 확인해주세요."),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "BLOG-0004", "주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있어요."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "BLOG-0005", "ID는 1 이상이어야 해요."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "BLOG-0006", "댓글은 1자 이상 1,000자 이하로 입력해주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "BLOG-0007", "닉네임은 1자 이상 20자 이하로 입력해주세요."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "BLOG-0008", "이 댓글에는 답글을 달 수 없어요."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0009", "삭제된 댓글에는 수정, 삭제, 좋아요를 할 수 없어요."),
    INVALID_COMMENT_SORT(HttpStatus.BAD_REQUEST, "BLOG-0011", "댓글 정렬 기준을 확인해주세요."),
    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "BLOG-0012", "회원 정보를 확인하지 못했어요. 다시 로그인해주세요."),
    INVALID_COMMENT_CURSOR(HttpStatus.BAD_REQUEST, "BLOG-0013", "댓글 목록을 불러오지 못했어요. 새로고침 후 다시 시도해주세요."),
    INVALID_CONTENT_TITLE(HttpStatus.BAD_REQUEST, "BLOG-0014", "제목은 1자 이상 200자 이하로 입력해주세요."),
    INVALID_CONTENT_SUMMARY(HttpStatus.BAD_REQUEST, "BLOG-0015", "요약은 500자 이하로 입력해주세요."),
    INVALID_THUMBNAIL_URL(HttpStatus.BAD_REQUEST, "BLOG-0016", "썸네일 URL은 1,000자 이하로 입력해주세요."),
    INVALID_CONTENT_BODY(HttpStatus.BAD_REQUEST, "BLOG-0017", "본문은 1자 이상 100,000자 이하로 입력해주세요."),
    INVALID_CONTENT_STATUS(HttpStatus.BAD_REQUEST, "BLOG-0018", "글 상태를 확인해주세요."),
    CONTENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "BLOG-0019", "이미 같은 주소의 글이 있어요. slug를 바꿔주세요."),
    CONTENT_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "BLOG-0020", "글을 공개한 뒤 다시 시도해주세요."),
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0021", "시리즈를 찾지 못했어요. 주소를 확인해주세요."),
    SERIES_ALREADY_EXISTS(HttpStatus.CONFLICT, "BLOG-0022", "이미 같은 주소의 시리즈가 있어요. slug를 바꿔주세요."),
    INVALID_SERIES_TITLE(HttpStatus.BAD_REQUEST, "BLOG-0023", "시리즈 제목은 1자 이상 200자 이하로 입력해주세요."),
    INVALID_SERIES_DESCRIPTION(HttpStatus.BAD_REQUEST, "BLOG-0024", "시리즈 설명은 1,000자 이하로 입력해주세요."),
    INVALID_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "BLOG-0025", "표시 순서는 0 이상으로 입력해주세요."),
    CONTENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "BLOG-0026", "시리즈와 글의 카테고리를 맞춰주세요."),
    HASHTAG_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOG-0027", "해시태그를 찾지 못했어요. 주소를 확인해주세요."),
    INVALID_HASHTAG(HttpStatus.BAD_REQUEST, "BLOG-0028", "해시태그는 공백 없이 1자 이상 30자 이하로 입력해주세요."),
    TOO_MANY_HASHTAGS(HttpStatus.BAD_REQUEST, "BLOG-0029", "해시태그는 10개 이하로 선택해주세요."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "BLOG-0030", "정렬 기준을 확인해주세요."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "BLOG-0031", "목록을 불러오지 못했어요. 새로고침 후 다시 시도해주세요."),
    CONTENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0032", "삭제된 글이에요. 목록에서 다시 선택해주세요."),
    SERIES_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "BLOG-0033", "삭제된 시리즈예요. 목록에서 다시 선택해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
