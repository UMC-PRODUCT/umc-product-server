package com.umc.product.member.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.umc.product.member.domain.MemberBlock;

public interface LoadMemberBlockPort {

    Optional<MemberBlock> findByBlockerAndBlocked(Long blockerMemberId, Long blockedMemberId);

    List<MemberBlock> listByBlockerMemberId(Long blockerMemberId);

    /**
     * blockerMemberId별 blockedMemberId 집합을 한 번의 조회로 반환한다. 차단한 대상이 없는
     * blockerMemberId는 결과 Map에서 빠진다.
     */
    Map<Long, Set<Long>> listBlockedMemberIdsByBlockerIds(Set<Long> blockerMemberIds);
}
