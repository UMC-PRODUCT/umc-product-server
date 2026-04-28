package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.CredentialPolicy;

/**
 * 비밀번호 변경 커맨드. 현재 비밀번호 검증 후 신규 비밀번호로 교체한다.
 */
public record ChangePasswordCommand(
    Long memberId,
    String currentRawPassword,
    String newRawPassword
) {
    public ChangePasswordCommand {
        // 현재 비밀번호는 인증 검증용이라 정책 위반 메시지는 노출하지 않고
        // 신규 비밀번호에 대해서만 정책을 강제한다.
        CredentialPolicy.validatePassword(newRawPassword);
    }

    public static ChangePasswordCommand of(Long memberId, String currentRawPassword, String newRawPassword) {
        return new ChangePasswordCommand(memberId, currentRawPassword, newRawPassword);
    }
}
