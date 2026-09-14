package com.umc.product.member.application.port.out;

import java.util.List;

import com.umc.product.member.domain.Member;

public interface SaveMemberPort {
    Member save(Member member);

    List<Member> saveAll(List<Member> members);

    void delete(Member member);
}
