package com.umc.product.member.adapter.out.persistence;

import com.umc.product.member.domain.Member;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("select m.id from Member m where m.schoolId = :schoolId")
    Set<Long> findAllIdsBySchoolId(Long schoolId);
}
