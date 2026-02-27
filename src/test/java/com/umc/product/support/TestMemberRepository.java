package com.umc.product.support;

import com.umc.product.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMemberRepository extends JpaRepository<Member, Long> {
}
