package com.umc.product.member.adapter.out.persistence;

import com.umc.product.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT m.id FROM Member m WHERE m.id > :lastId ORDER BY m.id ASC")
    List<Long> findIdsCursor(@Param("lastId") Long lastId, Pageable pageable);
}
