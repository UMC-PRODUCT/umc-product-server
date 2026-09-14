package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.Member;
import java.util.List;
import lombok.Builder;

/**
 * 이메일 기반 회원가입 커맨드. ADR-017 에 따라 loginId 없이 email 만 식별자로 사용한다.
 * <p>
 * email 은 회원가입 컨트롤러에서 emailVerificationToken 을 검증하여 추출한 값이다.
 */
@Builder
public record EmailRegisterMemberCommand(
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
