package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.Member;
import java.util.List;
import lombok.Builder;

@Builder
public record IdPwRegisterMemberCommand(
    String loginId,
    String rawPassword,

    String name,
    String nickname,
    String email,
    Long schoolId,

    List<TermConsents> termConsents
) {
    public Member toEntity() {
        return Member.create(name, nickname, email, schoolId, null);
    }
}
