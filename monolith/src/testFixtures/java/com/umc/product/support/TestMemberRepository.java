package com.umc.product.support;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.member.domain.Member;

public interface TestMemberRepository extends JpaRepository<Member, Long> {
}
