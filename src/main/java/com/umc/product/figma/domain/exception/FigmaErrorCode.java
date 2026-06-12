package com.umc.product.figma.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FigmaErrorCode implements BaseCode {

    INTEGRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0001", "Figma 연결 정보를 찾을 수 없어요. Figma 파일을 다시 연결해주세요."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0002", "Figma 연결에 실패했어요. 잠시 후 다시 시도해주세요."),
    OAUTH_TOKEN_REFRESH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0003", "Figma 연결이 만료됐어요. 다시 연결해주세요."),
    COMMENT_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0004", "Figma 댓글을 불러오지 못했어요. 잠시 후 다시 시도해주세요."),
    FILE_METADATA_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0005", "Figma 파일 정보를 불러오지 못했어요. 파일 키를 확인한 뒤 다시 시도해주세요."),
    WATCHED_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0006", "등록된 Figma 감시 파일이 아니에요. 감시 파일 목록을 확인해주세요."),
    WATCHED_FILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIGMA-0007", "이미 등록된 Figma 파일이에요. 기존 감시 파일을 확인해주세요."),
    OAUTH_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "FIGMA-0008", "Figma 연결 요청이 올바르지 않아요. 연결을 처음부터 다시 시도해주세요."),
    TOKEN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FIGMA-0009", "Figma 인증 정보를 저장하지 못했어요. 관리자에게 문의해주세요."),
    DISCORD_MENTION_SEND_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0010", "Discord 멘션을 보내지 못했어요. 잠시 후 다시 시도해주세요."),
    ROUTING_DOMAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0013", "Figma 라우팅 도메인을 찾을 수 없어요. 등록된 도메인을 확인해주세요."),
    ROUTING_DOMAIN_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIGMA-0014", "이미 등록된 라우팅 도메인이에요. 기존 도메인을 확인해주세요."),
    ROUTING_DOMAIN_MENTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0015", "이 라우팅 도메인의 멘션을 찾을 수 없어요. 멘션 설정을 확인해주세요."),
    ROUTING_DOMAIN_NOT_REGISTERED(HttpStatus.PRECONDITION_FAILED, "FIGMA-0016", "등록된 라우팅 도메인이 없어요. 라우팅 도메인을 먼저 등록해주세요."),
    DIGEST_RANGE_INVALID(HttpStatus.BAD_REQUEST, "FIGMA-0017", "요약할 기간이 올바르지 않아요. 시작과 종료 시간을 확인해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
