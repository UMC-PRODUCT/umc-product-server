package com.umc.product.member.application.port.in.command.dto;

/**
 * 회원 비밀번호 변경 커맨드.
 * <p>
 * 사용자 의도의 변경뿐 아니라 로그인 성공 시점의 점진적 rehash(transparent rehash)
 * 경로에서도 동일하게 사용된다.
 */
public record ChangeMemberPasswordCommand(
    Long memberId,
    String encodedPassword
) {
    public static ChangeMemberPasswordCommand of(Long memberId, String encodedPassword) {
        return new ChangeMemberPasswordCommand(memberId, encodedPassword);
    }
}
