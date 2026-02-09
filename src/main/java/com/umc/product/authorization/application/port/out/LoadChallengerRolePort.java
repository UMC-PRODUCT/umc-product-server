package com.umc.product.authorization.application.port.out;

import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;
import java.util.Set;

/**
 * Challenger의 Role 정보를 조회하는 Port
 */
public interface LoadChallengerRolePort {

    /**
     * 사용자의 모든 Role 조회
     *
     * @param memberId 사용자 ID
     * @return Role 리스트 (Role이 없으면 빈 리스트)
     */
    List<ChallengerRole> findByMemberId(Long memberId);

    /**
     * 특정 기수에서의 사용자 Role 조회
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @return Role 리스트
     */
    List<ChallengerRole> findRolesByMemberIdAndGisuId(Long memberId, Long gisuId);

    /**
     * 여러 챌린저의 Role 일괄 조회
     *
     * @param challengerIds 챌린저 ID 목록
     * @return Role 리스트
     */
    List<ChallengerRole> findByChallengerIdIn(Set<Long> challengerIds);
}
