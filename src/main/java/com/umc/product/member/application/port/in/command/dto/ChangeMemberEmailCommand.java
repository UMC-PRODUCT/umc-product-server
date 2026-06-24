package com.umc.product.member.application.port.in.command.dto;

/**
 * 이메일 인증 토큰으로 검증된 새 이메일을 현재 회원에게 반영하는 커맨드.
 */
public record ChangeMemberEmailCommand(
    Long memberId,
    String email
) {
    public static ChangeMemberEmailCommand of(Long memberId, String email) {
        return new ChangeMemberEmailCommand(memberId, email);
    }
}
