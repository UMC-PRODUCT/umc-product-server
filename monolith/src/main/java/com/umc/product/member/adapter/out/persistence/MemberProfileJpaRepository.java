package com.umc.product.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.member.domain.MemberProfile;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfile, Long> {
}
