package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.ChangeMemberEmailCommand;

/**
 * 회원의 이메일을 변경하는 UseCase.
 */
public interface ChangeMemberEmailUseCase {

    void changeEmail(ChangeMemberEmailCommand command);
}
