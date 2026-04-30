package com.umc.product.member.application.port.in.command.dto;

/**
 * 회원에게 ID/PW 자격증명을 최초로 부착하는 커맨드.
 * <p>
 * {@code encodedPassword} 는 반드시 DelegatingPasswordEncoder 가 생성한
 * "{id}encoded" 형태여야 한다. 평문 비밀번호 / 형식 검증은 Auth 도메인에서 수행한다.
 */
public record RegisterMemberCredentialCommand(
    Long memberId,
    String loginId,
    String encodedPassword
) {
    public static RegisterMemberCredentialCommand of(Long memberId, String loginId, String encodedPassword) {
        return new RegisterMemberCredentialCommand(memberId, loginId, encodedPassword);
    }
}
