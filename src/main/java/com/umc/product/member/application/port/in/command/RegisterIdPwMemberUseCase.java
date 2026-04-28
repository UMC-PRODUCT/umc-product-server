package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;

public interface RegisterIdPwMemberUseCase {
    Long register(IdPwRegisterMemberCommand command);
}
