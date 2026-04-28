package com.umc.product.authentication.application.port.in.command.dto;

import lombok.Builder;

/**
 * ID/PW 로그인 성공 결과. JWT 발급은 Service 가 담당하며 본 DTO 는 그 결과만 운반한다.
 */
@Builder
public record IdPwLoginResult(
    Long memberId,
    String accessToken,
    String refreshToken
) {
}
