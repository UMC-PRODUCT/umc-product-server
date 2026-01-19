package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.Member;

public interface SaveMemberPort {
    Member save(Member member);

    void delete(Member member);
}
