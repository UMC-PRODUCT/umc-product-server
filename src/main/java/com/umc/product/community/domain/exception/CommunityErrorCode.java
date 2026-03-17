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

    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "COMMUNITY-0401", "이미 신고한 게시글/댓글입니다."),

    // === Post 검증 ===
    INVALID_POST_AUTHOR(HttpStatus.BAD_REQUEST, "COMMUNITY-0107", "작성자 ID는 필수입니다."),
    NOT_LIGHTNING_POST(HttpStatus.BAD_REQUEST, "COMMUNITY-0108", "번개 게시글이 아닙니다."),
    USE_LIGHTNING_API(HttpStatus.BAD_REQUEST, "COMMUNITY-0109", "번개 게시글은 번개 전용 API를 사용하세요."),
    LIGHTNING_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "COMMUNITY-0110", "번개 게시글은 추가 정보가 필수입니다."),
    POST_NOT_OWNED(HttpStatus.FORBIDDEN, "COMMUNITY-0111", "본인의 게시글만 수정/삭제할 수 있습니다."),

    // === Lightning 검증 ===
    INVALID_LIGHTNING_MEET_AT(HttpStatus.BAD_REQUEST, "COMMUNITY-0151", "모임 시간은 필수입니다."),
    INVALID_LIGHTNING_LOCATION(HttpStatus.BAD_REQUEST, "COMMUNITY-0152", "모임 장소는 필수입니다."),
    INVALID_LIGHTNING_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "COMMUNITY-0153", "최대 참가자는 1명 이상이어야 합니다."),
    INVALID_LIGHTNING_OPEN_CHAT_URL(HttpStatus.BAD_REQUEST, "COMMUNITY-0154", "오픈 채팅 링크는 필수입니다."),
    INVALID_LIGHTNING_OPEN_CHAT_URL_FORMAT(HttpStatus.BAD_REQUEST, "COMMUNITY-0155", "오픈 채팅 링크는 http:// 또는 https://로 시작해야 합니다."),
    INVALID_LIGHTNING_MEET_AT_PAST(HttpStatus.BAD_REQUEST, "COMMUNITY-0156", "모임 시간은 현재 이후여야 합니다."),

    // === Comment 검증 ===
    INVALID_COMMENT_POST_ID(HttpStatus.BAD_REQUEST, "COMMUNITY-0203", "게시글 ID는 필수입니다."),
    INVALID_COMMENT_CHALLENGER_ID(HttpStatus.BAD_REQUEST, "COMMUNITY-0204", "챌린저 ID는 필수입니다."),

    // === 공통 ID 검증 ===
    INVALID_ID(HttpStatus.BAD_REQUEST, "COMMUNITY-0901", "ID는 양수여야 합니다."),

    // === Adapter 검증 ===
    POST_SAVE_REQUIRES_AUTHOR(HttpStatus.BAD_REQUEST, "COMMUNITY-0902", "새 게시글 생성 시에는 작성자 정보가 필요합니다."),
    POST_UPDATE_INVALID_CALL(HttpStatus.BAD_REQUEST, "COMMUNITY-0903", "이미 ID가 있는 게시글은 update용 save를 사용하세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
