package com.umc.product.figma.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FigmaErrorCode implements BaseCode {

    INTEGRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0001", "Figma OAuth 통합 정보가 등록되어 있지 않습니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0002", "Figma OAuth 토큰 교환에 실패했습니다."),
    OAUTH_TOKEN_REFRESH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0003", "Figma access token 갱신에 실패했습니다."),
    COMMENT_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0004", "Figma 댓글 조회에 실패했습니다."),
    FILE_METADATA_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0005", "Figma 파일 메타데이터 조회에 실패했습니다."),
    WATCHED_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0006", "등록된 Figma 폴링 대상 파일이 아닙니다."),
    WATCHED_FILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIGMA-0007", "이미 등록된 Figma 파일 키 입니다."),
    OAUTH_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "FIGMA-0008", "Figma OAuth state 값이 일치하지 않습니다."),
    TOKEN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FIGMA-0009", "Figma 토큰 암복호화에 실패했습니다."),
    DISCORD_MENTION_SEND_FAILED(HttpStatus.BAD_GATEWAY, "FIGMA-0010", "Discord 멘션 전송에 실패했습니다."),
    ROUTING_DOMAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0013", "등록된 Figma 라우팅 도메인이 아닙니다."),
    ROUTING_DOMAIN_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIGMA-0014", "동일한 domain_key 의 라우팅 도메인이 이미 등록되어 있습니다."),
    ROUTING_DOMAIN_MENTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FIGMA-0015", "해당 라우팅 도메인의 mention 이 아닙니다."),
    ROUTING_DOMAIN_NOT_REGISTERED(HttpStatus.PRECONDITION_FAILED, "FIGMA-0016", "라우팅 도메인이 한 건도 등록되어 있지 않습니다."),
    DIGEST_RANGE_INVALID(HttpStatus.BAD_REQUEST, "FIGMA-0017", "digest 의 from/to 시간창이 유효하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
