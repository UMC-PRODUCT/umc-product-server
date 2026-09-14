package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.Member;
import java.util.List;

public interface SaveMemberPort {
    Member save(Member member);

    List<Member> saveAll(List<Member> members);

    void delete(Member member);
}
