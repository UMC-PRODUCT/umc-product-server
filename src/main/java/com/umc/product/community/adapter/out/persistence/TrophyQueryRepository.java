package com.umc.product.community.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.community.adapter.out.persistence.QTrophyJpaEntity.trophyJpaEntity;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.trophy.query.TrophySearchQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrophyQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 트로피 검색 (week, school, part 필터링)
     * <p>
     * DB 레벨에서 필터링하여 필요한 데이터만 조회합니다.
     * Trophy -> Challenger -> Member -> School 조인을 통해 school과 part 필터링을 수행합니다.
     */
    public List<TrophyJpaEntity> findAllByQuery(TrophySearchQuery query) {
        BooleanBuilder condition = buildSearchCondition(query);

        return queryFactory
                .selectFrom(trophyJpaEntity)
                .leftJoin(challenger).on(trophyJpaEntity.challengerId.eq(challenger.id))
                .leftJoin(member).on(challenger.memberId.eq(member.id))
                .leftJoin(school).on(member.schoolId.eq(school.id))
                .where(condition)
                .orderBy(trophyJpaEntity.id.desc())
                .fetch();
    }

    /**
     * 동적 쿼리 조건 생성
     */
    private BooleanBuilder buildSearchCondition(TrophySearchQuery query) {
        BooleanBuilder builder = new BooleanBuilder();

        // week 필터링
        builder.and(weekEq(query.week()));

        // school 필터링 (member 테이블 조인 필요)
        builder.and(schoolNameEq(query.school()));

        // part 필터링 (challenger 테이블 조인 필요)
        builder.and(partEq(query.part()));

        return builder;
    }

    /**
     * week 조건
     */
    private BooleanExpression weekEq(Integer week) {
        return week != null ? trophyJpaEntity.week.eq(week) : null;
    }

    /**
     * school 조건 (학교명으로 필터링)
     */
    private BooleanExpression schoolNameEq(String schoolName) {
        if (schoolName == null || schoolName.isBlank()) {
            return null;
        }
        return school.name.eq(schoolName);
    }

    /**
     * part 조건 (파트명으로 필터링)
     */
    private BooleanExpression partEq(String part) {
        if (part == null || part.isBlank()) {
            return null;
        }

        try {
            ChallengerPart challengerPart = ChallengerPart.valueOf(part.toUpperCase());
            return challenger.part.eq(challengerPart);
        } catch (IllegalArgumentException e) {
            // 잘못된 part 값이면 조건을 추가하지 않음
            return null;
        }
    }
}
