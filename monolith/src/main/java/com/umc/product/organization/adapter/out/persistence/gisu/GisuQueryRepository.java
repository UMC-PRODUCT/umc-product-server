package com.umc.product.organization.adapter.out.persistence.gisu;

import static com.umc.product.organization.domain.QGisu.gisu;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.domain.Gisu;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GisuQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Optional<Gisu> findGisuByDate(Instant targetDate) {
        return Optional.ofNullable(
            jpaQueryFactory.selectFrom(gisu)
                .where(
                    gisu.period.startAt.loe(targetDate), // startAt <= targetDate
                    gisu.period.endAt.goe(targetDate) // endAt >= targetDate
                )
                .fetchFirst()
        );
    }
}
