package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;

/**
 * 이메일 기반 회원가입 UseCase. ADR-017 흐름에서 사용한다.
 */
public interface RegisterEmailMemberUseCase {
    Long register(EmailRegisterMemberCommand command);
}
