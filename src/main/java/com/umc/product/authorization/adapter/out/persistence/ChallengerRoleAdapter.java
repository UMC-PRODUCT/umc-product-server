package com.umc.product.authorization.adapter.out.persistence;

import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ChallengerRole Persistence Adapter
 */
@Component
@RequiredArgsConstructor
public class ChallengerRoleAdapter implements LoadChallengerRolePort {

    private final ChallengerRoleJpaRepository jpaRepository;
    private final ChallengerRoleQueryRepository queryRepository;

    @Override
    public List<ChallengerRole> findByMemberId(Long memberId) {
        return queryRepository.findByMemberId(memberId);
    }

    @Override
    public List<ChallengerRole> findRolesByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return queryRepository.findByMemberIdAndGisuId(memberId, gisuId);
    }

    @Override
    public List<ChallengerRole> findByChallengerIdIn(Set<Long> challengerIds) {
        return jpaRepository.findByChallengerIdIn(challengerIds);
    }
}
