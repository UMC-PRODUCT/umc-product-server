package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.organization.domain.UmcProductMember;

public interface UmcProductMemberJpaRepository extends JpaRepository<UmcProductMember, Long> {

    Optional<UmcProductMember> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    List<UmcProductMember> findByIdIn(Collection<Long> ids);
}
