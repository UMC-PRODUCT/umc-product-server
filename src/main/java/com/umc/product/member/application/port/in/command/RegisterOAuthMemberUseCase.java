package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import java.util.List;

public interface RegisterOAuthMemberUseCase {
    /**
     * 회원가입 (회원 생성) 입니다. OAuth를 이용해서 회원가입하는 경우만을 고려합니다.
     * <p>
     * register complete라고 보시면 됩니다.
     */
    Long register(RegisterMemberCommand command);

    List<Long> batchRegister(List<RegisterMemberCommand> commands);
}
