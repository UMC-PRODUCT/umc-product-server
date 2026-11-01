package com.umc.product.member.application.port.in.command.dto;

/**
 * 이메일 기반 자격증명을 회원에게 최초로 부착하는 커맨드. ADR-017 흐름에서 사용한다.
 * <p>
 * email 은 이미 Member.email 에 저장되어 있으므로, 본 커맨드는 비밀번호 해시만 전달한다.
 * {@code encodedPassword} 는 반드시 DelegatingPasswordEncoder 가 생성한
 * "{id}encoded" 형태여야 한다.
 */
public record RegisterMemberCredentialByEmailCommand(
    Long memberId,
    String encodedPassword
) {
    public static RegisterMemberCredentialByEmailCommand of(Long memberId, String encodedPassword) {
        return new RegisterMemberCredentialByEmailCommand(memberId, encodedPassword);
    }
}
