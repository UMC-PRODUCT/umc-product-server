package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.member.domain.Member;

/**
 * 이메일/PW 인증 흐름에서 사용하는 자격증명 조회용 DTO. ADR-017 흐름.
 * <p>
 * 비밀번호 해시는 Spring DelegatingPasswordEncoder 의 "{id}encoded" prefix 를 포함한다.
 * passwordEncoder.matches / upgradeEncoding 호출에 그대로 전달할 수 있다.
 */
public record MemberCredentialInfo(
    Long memberId,
    String passwordHash
) {
    public static MemberCredentialInfo from(Member member) {
        return new MemberCredentialInfo(
            member.getId(),
            member.getPasswordHash()
        );
    }

    @Override
    public String toString() {
        // 해시 자체도 민감 정보로 보고 마스킹한다.
        return "MemberCredentialInfo[memberId=" + memberId + ", passwordHash=***]";
    }
}
