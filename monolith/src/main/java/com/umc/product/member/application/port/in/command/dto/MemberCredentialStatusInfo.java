package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.Member;

/**
 * 로그인 수단 정책 판단에 사용하는 local credential 상태 DTO.
 * <p>
 * 비밀번호 해시를 외부 도메인에 노출하지 않고 등록 여부만 전달한다.
 */
public record MemberCredentialStatusInfo(
    Long memberId,
    boolean hasCredential
) {
    public static MemberCredentialStatusInfo from(Member member) {
        return new MemberCredentialStatusInfo(
            member.getId(),
            member.hasCredential()
        );
    }
}
