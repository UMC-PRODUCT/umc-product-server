package com.umc.product.authorization.adapter.out.persistence;

import static com.umc.product.authorization.domain.QChallengerRole.challengerRole;
import static com.umc.product.challenger.domain.QChallenger.challenger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.common.domain.enums.ChallengerRoleType;

import lombok.RequiredArgsConstructor;

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

    public Map<Long, List<RoleAttribute>> mapRolesByMemberIncludingCentralCore(Long gisuId) {
        return queryFactory.select(challenger.memberId, challengerRole)
            .from(challengerRole)
            .join(challenger).on(challenger.id.eq(challengerRole.challengerId))
            .where(gisuId == null ? null : challengerRole.gisuId.eq(gisuId)
                .or(challengerRole.challengerRoleType.in(
                    ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerRoleType.CENTRAL_VICE_PRESIDENT)))
            .fetch().stream()
            .collect(Collectors.groupingBy(row -> Objects.requireNonNull(row.get(challenger.memberId)),
                Collectors.mapping(row -> RoleAttribute.from(Objects.requireNonNull(row.get(challengerRole))),
                    Collectors.toList())));
    }
}
