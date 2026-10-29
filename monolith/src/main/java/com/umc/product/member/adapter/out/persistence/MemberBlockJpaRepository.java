package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.member.domain.MemberBlock;

public interface MemberBlockJpaRepository extends JpaRepository<MemberBlock, Long> {

    Optional<MemberBlock> findByBlockerMemberIdAndBlockedMemberId(Long blockerMemberId, Long blockedMemberId);

    List<MemberBlock> findAllByBlockerMemberId(Long blockerMemberId);

    List<MemberBlock> findAllByBlockerMemberIdIn(Set<Long> blockerMemberIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MemberBlock b WHERE b.blockerMemberId = :blockerMemberId AND b.blockedMemberId = :blockedMemberId")
    int deleteByBlockerMemberIdAndBlockedMemberId(
        @Param("blockerMemberId") Long blockerMemberId,
        @Param("blockedMemberId") Long blockedMemberId
    );

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MemberBlock b WHERE b.blockerMemberId = :memberId OR b.blockedMemberId = :memberId")
    int deleteAllInvolvingMember(@Param("memberId") Long memberId);
}
