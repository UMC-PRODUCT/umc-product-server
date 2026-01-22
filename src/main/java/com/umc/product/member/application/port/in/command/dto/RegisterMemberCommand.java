package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.domain.Member;
import java.util.List;
import lombok.Builder;

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
@Builder
public record RegisterMemberCommand(
        OAuthProvider provider,
        String providerId,
        String name,
        String nickname,
        String email,
        Long schoolId,
        Long profileImageId,
        List<TermConsents> termConsents
) {

    public Member toEntity() {
        return Member.builder()
                .name(this.name)
                .nickname(this.nickname)
                .email(this.email)
                .schoolId(this.schoolId)
                .profileImageId(this.profileImageId)
                .build();
    }
}
