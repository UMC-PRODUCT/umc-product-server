package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * OAuth 회원가입용 Command입니다. 일반 회원가입은 지원하지 않습니다.
 * <p>
 * RegisterToken을 사용하는 경우, 해당 토큰에서 해석한 값을 반영해서 집어넣습니다.
 *
 * @param provider
 * @param providerId
 * @param name
 * @param nickname
 * @param email
 * @param schoolId
 * @param profileImageId
 */
public record RegisterMemberCommand(
        OAuthProvider provider,
        String providerId,
        String name,
        String nickname,
        String email,
        String schoolId,
        String profileImageId
) {
}
