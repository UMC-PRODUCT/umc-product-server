package com.umc.product.member.adapter.out.persistence;

import com.umc.product.member.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfile, Long> {
}