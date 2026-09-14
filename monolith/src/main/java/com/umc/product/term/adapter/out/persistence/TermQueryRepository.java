package com.umc.product.term.adapter.out.persistence;

import static com.umc.product.term.domain.QTerm.term;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TermQueryRepository {

    private final JPAQueryFactory queryFactory;


    /**
     * 특정 타입의 활성화된 약관을 최신순으로 조회합니다.
     */
    public Optional<Term> findActiveByType(TermType type) {
        return Optional.ofNullable(
            queryFactory.selectFrom(term)
                .where(
                    term.type.eq(type),
                    term.active.isTrue()
                )
                .fetchFirst()
        );
    }

    public List<Term> findAllActiveRequired() {
        return queryFactory.selectDistinct(term)
            .from(term)
            .where(
                term.active.eq(true),
                term.required.eq(true)
            )
            .fetch();
    }
}
