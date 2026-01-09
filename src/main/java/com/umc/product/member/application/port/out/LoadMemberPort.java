package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.Member;
import java.util.Optional;

public interface LoadMemberPort {
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
