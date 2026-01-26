package com.umc.product.terms.adapter.out.persistence;

import static com.umc.product.terms.domain.QTerms.terms;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TermsQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 타입의 활성화된 약관을 최신순으로 조회합니다.
     */
    public Optional<Terms> findActiveByType(TermsType type) {
        return Optional.ofNullable(
                queryFactory.selectFrom(terms)
                        .where(
                                terms.type.eq(type),
                                terms.active.isTrue()
                        )
                        .orderBy(terms.effectiveDate.desc())
                        .fetchFirst()
        );
    }

    public List<Terms> findAllActiveRequired() {
        return queryFactory.selectDistinct(terms)
                .from(terms)
                .where(
                        terms.active.eq(true),
                        terms.required.eq(true)
                )
                .fetch();
    }
}
