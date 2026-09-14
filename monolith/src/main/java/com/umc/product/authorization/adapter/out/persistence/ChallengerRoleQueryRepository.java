package com.umc.product.authorization.adapter.out.persistence;

import static com.umc.product.authorization.domain.QChallengerRole.challengerRole;
import static com.umc.product.challenger.domain.QChallenger.challenger;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ChallengerRole QueryDSL Repository
 */
@Repository
@RequiredArgsConstructor
public class ChallengerRoleQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * memberId로 모든 Role 조회
     *
     * @param memberId 사용자 ID
     * @return Role 리스트
     */
    public List<ChallengerRole> findByMemberId(Long memberId) {
        return queryFactory
            .selectFrom(challengerRole)
            .join(challenger).on(challengerRole.challengerId.eq(challenger.id))
            .where(challenger.memberId.eq(memberId))
            .fetch();
    }

    /**
     * memberId와 gisuId로 Role 조회
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @return Role 리스트
     */
    public List<ChallengerRole> findByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return queryFactory
            .selectFrom(challengerRole)
            .join(challenger).on(challengerRole.challengerId.eq(challenger.id))
            .where(
                challenger.memberId.eq(memberId),
                challengerRole.gisuId.eq(gisuId)
            )
            .fetch();
    }
}
