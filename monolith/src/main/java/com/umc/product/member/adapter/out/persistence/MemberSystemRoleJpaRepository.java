package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;

public interface MemberSystemRoleJpaRepository extends JpaRepository<MemberSystemRole, Long> {

    List<MemberSystemRole> findAllByMemberId(Long memberId);

    @Query("select role.memberId from MemberSystemRole role where role.roleType = :roleType")
    Set<Long> findMemberIdsByRoleType(@Param("roleType") MemberSystemRoleType roleType);
}
