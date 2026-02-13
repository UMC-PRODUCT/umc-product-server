package com.umc.product.challenger.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
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

    public Page<Challenger> pagingSearch(SearchChallengerQuery query, Pageable pageable) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildOffsetSearchCondition(query, challenger, member);

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

    public List<Challenger> cursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildOffsetSearchCondition(query, challenger, member);

        if (cursor != null) {
            condition.and(buildCursorSearchCondition(cursor, challenger, member));
        }

        return queryFactory
                .selectFrom(challenger)
                .join(member).on(challenger.memberId.eq(member.id))
                .where(condition)
                .orderBy(partOrder(challenger).asc(), challenger.gisuId.desc(), member.name.asc(), challenger.id.asc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * 파트별 챌린저 수
     */
    public Map<ChallengerPart, Long> countByPart(SearchChallengerQuery query) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildOffsetSearchCondition(query, challenger, member);

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

    /**
     * 챌린저별 포인트 합계
     */
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


    // ========== private methods ==========
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

    private BooleanBuilder buildOffsetSearchCondition(
            SearchChallengerQuery query,
            QChallenger challenger,
            QMember member
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(challengerIdEq(query.challengerId(), challenger));
        builder.and(nicknameOrNameContains(query.nickname(), query.name(), member));
        builder.and(schoolIdEq(query.schoolId(), member));
        builder.and(chapterIdExists(query.chapterId(), member));
        builder.and(partEq(query.part(), challenger));
        builder.and(gisuIdEq(query.gisuId(), challenger));
        builder.and(statusIn(query.statuses(), challenger));

        return builder;
    }

    private BooleanExpression buildCursorSearchCondition(Long cursorId, QChallenger challenger, QMember member) {
        QChallenger c = new QChallenger("cursorC");
        QMember m = new QMember("cursorM");

        // 커서 챌린저의 정렬 키 값 조회
        Tuple cursorRow = queryFactory
            .select(partOrder(c), c.gisuId, m.name, c.id)
            .from(c)
            .join(m).on(c.memberId.eq(m.id))
            .where(c.id.eq(cursorId))
            .fetchOne();

        if (cursorRow == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CURSOR_ID);
        }

        Integer cursorPartOrder = cursorRow.get(partOrder(c));
        Long cursorGisuId = cursorRow.get(c.gisuId);
        String cursorName = cursorRow.get(m.name);
        Long cursorIdVal = cursorRow.get(c.id);

        NumberExpression<Integer> currentPartOrder = partOrder(challenger);

        // keyset pagination: 정렬 순서(partOrder ASC, gisuId DESC, name ASC, id ASC) 기반
        return currentPartOrder.gt(cursorPartOrder)
            .or(currentPartOrder.eq(cursorPartOrder)
                .and(challenger.gisuId.lt(cursorGisuId)))
            .or(currentPartOrder.eq(cursorPartOrder)
                .and(challenger.gisuId.eq(cursorGisuId))
                .and(member.name.gt(cursorName)))
            .or(currentPartOrder.eq(cursorPartOrder)
                .and(challenger.gisuId.eq(cursorGisuId))
                .and(member.name.eq(cursorName))
                .and(challenger.id.gt(cursorIdVal)));
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

    private BooleanExpression nicknameOrNameContains(String nickname, String name, QMember member) {
        BooleanExpression nicknameCondition = (nickname != null && !nickname.isBlank())
                ? member.nickname.containsIgnoreCase(nickname) : null;
        BooleanExpression nameCondition = (name != null && !name.isBlank())
                ? member.name.containsIgnoreCase(name) : null;

        if (nicknameCondition != null && nameCondition != null) {
            return nicknameCondition.or(nameCondition);
        }
        if (nicknameCondition != null) {
            return nicknameCondition;
        }
        return nameCondition;
    }

    private BooleanExpression schoolIdEq(Long schoolId, QMember member) {
        return schoolId != null ? member.schoolId.eq(schoolId) : null;
    }

    private BooleanExpression statusIn(List<ChallengerStatus> statuses, QChallenger challenger) {
        return (statuses != null && !statuses.isEmpty()) ? challenger.status.in(statuses) : null;
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
