package com.umc.product.challenger.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallengerPoint.challengerPoint;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.domain.ChallengerPoint;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengerPointQueryRepository {

    private final JPAQueryFactory queryFactory;

    List<ChallengerPoint> findAllByChallenger(Long challengerId) {
        return queryFactory
            .select(challengerPoint)
            .from(challengerPoint)
            .where(challengerPoint.challenger.id.eq(challengerId))
            .fetch();
    }

    public List<ChallengerPoint> findAllByChallengerIdIn(Set<Long> challengerIds) {
        return queryFactory
            .select(challengerPoint)
            .from(challengerPoint)
            .where(challengerPoint.challenger.id.in(challengerIds))
            .fetch();
    }

}
