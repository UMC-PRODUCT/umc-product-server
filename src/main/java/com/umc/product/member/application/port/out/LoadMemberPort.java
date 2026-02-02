package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.Member;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadMemberPort {
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    List<Member> findByIdIn(Set<Long> ids);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
