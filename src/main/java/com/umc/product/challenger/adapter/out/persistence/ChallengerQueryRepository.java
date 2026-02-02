package com.umc.product.challenger.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.application.port.in.query.SearchChallengerQuery;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.domain.QMember;
import com.umc.product.organization.domain.QChapterSchool;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengerQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Challenger> search(SearchChallengerQuery query, Pageable pageable) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildSearchCondition(query, challenger, member);

        List<Challenger> content = queryFactory
                .selectFrom(challenger)
                .join(member).on(challenger.memberId.eq(member.id))
                .where(condition)
                .orderBy(partOrder(challenger).asc(), challenger.gisuId.desc(), member.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(challenger.count())
                .from(challenger)
                .join(member).on(challenger.memberId.eq(member.id))
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public Map<ChallengerPart, Long> countByPart(SearchChallengerQuery query) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildSearchCondition(query, challenger, member);

        List<Tuple> tuples = queryFactory
                .select(challenger.part, challenger.count())
                .from(challenger)
                .join(member).on(challenger.memberId.eq(member.id))
                .where(condition)
                .groupBy(challenger.part)
                .fetch();

        return tuples.stream()
                .filter(tuple -> tuple.get(challenger.part) != null)
                .collect(Collectors.toMap(
                        tuple -> Objects.requireNonNull(tuple.get(challenger.part)),
                        tuple -> Objects.requireNonNull(tuple.get(challenger.count()))
                ));
    }

    public Map<Long, Double> sumPointsByChallengerIds(Set<Long> challengerIds) {
        if (challengerIds == null || challengerIds.isEmpty()) {
            return Map.of();
        }

        QChallengerPoint point = QChallengerPoint.challengerPoint;

        NumberExpression<Double> pointValue = new CaseBuilder()
                .when(point.type.eq(PointType.WARNING)).then(0.5)
                .when(point.type.eq(PointType.OUT)).then(1.0)
                .otherwise(0.0);

        List<Tuple> tuples = queryFactory
                .select(point.challenger.id, pointValue.sum())
                .from(point)
                .where(point.challenger.id.in(challengerIds))
                .groupBy(point.challenger.id)
                .fetch();

        return tuples.stream()
                .filter(tuple -> tuple.get(point.challenger.id) != null)
                .collect(Collectors.toMap(
                        tuple -> Objects.requireNonNull(tuple.get(point.challenger.id)),
                        tuple -> {
                            Double value = tuple.get(pointValue.sum());
                            return value != null ? value : 0.0;
                        }
                ));
    }

    private NumberExpression<Integer> partOrder(QChallenger challenger) {
        return new CaseBuilder()
                .when(challenger.part.eq(ChallengerPart.PLAN)).then(ChallengerPart.PLAN.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.DESIGN)).then(ChallengerPart.DESIGN.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.WEB)).then(ChallengerPart.WEB.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.ANDROID)).then(ChallengerPart.ANDROID.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.IOS)).then(ChallengerPart.IOS.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.NODEJS)).then(ChallengerPart.NODEJS.getSortOrder())
                .when(challenger.part.eq(ChallengerPart.SPRINGBOOT)).then(ChallengerPart.SPRINGBOOT.getSortOrder())
                .otherwise(999);
    }

    private BooleanBuilder buildSearchCondition(
            SearchChallengerQuery query,
            QChallenger challenger,
            QMember member
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(challengerIdEq(query.challengerId(), challenger));
        builder.and(nicknameOrNameContains(query.nickname(), member));
        builder.and(schoolIdEq(query.schoolId(), member));
        builder.and(chapterIdExists(query.chapterId(), member));
        builder.and(partEq(query.part(), challenger));
        builder.and(gisuIdEq(query.gisuId(), challenger));

        return builder;
    }

    private BooleanExpression challengerIdEq(Long challengerId, QChallenger challenger) {
        return challengerId != null ? challenger.id.eq(challengerId) : null;
    }

    private BooleanExpression gisuIdEq(Long gisuId, QChallenger challenger) {
        return gisuId != null ? challenger.gisuId.eq(gisuId) : null;
    }

    private BooleanExpression partEq(ChallengerPart part, QChallenger challenger) {
        return part != null ? challenger.part.eq(part) : null;
    }

    private BooleanExpression nicknameOrNameContains(String value, QMember member) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return member.nickname.containsIgnoreCase(value)
                .or(member.name.containsIgnoreCase(value));
    }

    private BooleanExpression schoolIdEq(Long schoolId, QMember member) {
        return schoolId != null ? member.schoolId.eq(schoolId) : null;
    }

    private BooleanExpression chapterIdExists(Long chapterId, QMember member) {
        if (chapterId == null) {
            return null;
        }

        QChapterSchool chapterSchool = QChapterSchool.chapterSchool;

        return JPAExpressions
                .selectOne()
                .from(chapterSchool)
                .where(
                        chapterSchool.school.id.eq(member.schoolId),
                        chapterSchool.chapter.id.eq(chapterId)
                )
                .exists();
    }
}
