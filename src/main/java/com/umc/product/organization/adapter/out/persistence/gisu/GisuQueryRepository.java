package com.umc.product.organization.adapter.out.persistence.gisu;

import static com.umc.product.organization.domain.QGisu.gisu;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.domain.Gisu;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
