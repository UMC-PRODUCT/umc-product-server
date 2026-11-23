package com.umc.product.challenger.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.challenger.domain.QChallengerDeactivation.challengerDeactivation;
import static com.umc.product.challenger.domain.QChallengerPoint.challengerPoint;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QSchool.school;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.out.SearchChallengerDeactivationPort;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationListStatus;
import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChallengerDeactivationQueryRepository implements SearchChallengerDeactivationPort {

    private static final double CANDIDATE_POINT_THRESHOLD = -20.0;
    private static final Set<ChallengerStatus> DEACTIVATED_STATUSES =
        Set.of(ChallengerStatus.WITHDRAWN, ChallengerStatus.EXPELLED);

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ChallengerDeactivationListItemInfo> search(SearchChallengerDeactivationQuery query, Pageable pageable) {
        BooleanBuilder conditions = searchConditions(query);
        List<ChallengerDeactivationListItemInfo> rows = withListJoins(queryFactory.select(
                challenger.id, challenger.status, challenger.gisuId, challenger.part,
                member.name, member.nickname, member.profileImageId, member.schoolId, school.name,
                challengerDeactivation.id, challengerDeactivation.status, challengerDeactivation.reason,
                challengerDeactivation.createdAt
            ))
            .leftJoin(school).on(school.id.eq(member.schoolId))
            .where(conditions)
            .orderBy(orderBy(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch().stream()
            .map(this::toListItem)
            .toList();

        return PageableExecutionUtils.getPage(
            rows, pageable, () -> count(withListJoins(queryFactory.select(challenger.count())).where(conditions))
        );
    }

    private <T> JPAQuery<T> withListJoins(JPAQuery<T> query) {
        return query.from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(challengerDeactivation).on(challengerDeactivation.challengerId.eq(challenger.id));
    }

    private long count(JPAQuery<Long> query) {
        Long total = query.fetchOne();
        return total == null ? 0 : total;
    }

    private BooleanBuilder searchConditions(SearchChallengerDeactivationQuery query) {
        BooleanBuilder conditions = new BooleanBuilder(challenger.gisuId.eq(query.gisuId()));
        BooleanExpression registered = challengerDeactivation.id.isNotNull()
            .or(challenger.status.in(DEACTIVATED_STATUSES));
        conditions.and(switch (query.status()) {
            case PENDING -> pending(query.gisuId());
            case REGISTERED -> registered;
            case null -> registered.or(pending(query.gisuId()));
        });

        if (query.chapterId() != null) {
            conditions.and(JPAExpressions.selectOne()
                .from(chapterSchool)
                .where(
                    chapterSchool.chapter.id.eq(query.chapterId()),
                    chapterSchool.chapter.gisu.id.eq(challenger.gisuId),
                    chapterSchool.school.id.eq(member.schoolId)
                )
                .exists());
        }
        if (query.schoolId() != null) {
            conditions.and(member.schoolId.eq(query.schoolId()));
        }
        if (query.part() != null) {
            conditions.and(challenger.part.eq(query.part()));
        }
        if (query.reason() != null) {
            conditions.and(challengerDeactivation.reason.eq(query.reason()));
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().strip();
            conditions.and(member.name.containsIgnoreCase(keyword)
                .or(member.nickname.containsIgnoreCase(keyword))
                .or(member.email.containsIgnoreCase(keyword)));
        }
        return conditions;
    }

    private BooleanExpression pending(Long gisuId) {
        // 직접 입력 점수와 과거 저장 배점이 있으면 기존 상벌점 조회와 동일하게 우선 적용한다.
        NumberExpression<Double> point = challengerPoint.pointValue.doubleValue()
            .coalesce(ChallengerQueryRepository.defaultPointValue(challengerPoint));
        BooleanExpression meetsThreshold = challenger.id.in(
            JPAExpressions.select(challengerPoint.challenger.id)
                .from(challengerPoint)
                .where(challengerPoint.challenger.gisuId.eq(gisuId))
                .groupBy(challengerPoint.challenger.id)
                .having(point.sum().loe(CANDIDATE_POINT_THRESHOLD))
        );
        return challengerDeactivation.id.isNull()
            .and(challenger.status.eq(ChallengerStatus.ACTIVE))
            .and(meetsThreshold);
    }

    private OrderSpecifier<?>[] orderBy(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            switch (order.getProperty()) {
                case "name" -> orders.add(order.isAscending() ? member.name.asc() : member.name.desc());
                case "registeredAt" -> orders.add(order.isAscending()
                    ? challengerDeactivation.createdAt.asc().nullsLast()
                    : challengerDeactivation.createdAt.desc().nullsLast());
                default -> {
                }
            }
        });
        if (orders.isEmpty()) {
            orders.add(challengerDeactivation.createdAt.desc().nullsLast());
        }
        orders.add(challenger.id.desc());
        return orders.toArray(OrderSpecifier<?>[]::new);
    }

    private ChallengerDeactivationListItemInfo toListItem(Tuple row) {
        Long deactivationId = row.get(challengerDeactivation.id);
        ChallengerStatus status = deactivationId == null
            ? row.get(challenger.status)
            : row.get(challengerDeactivation.status);
        boolean registered = deactivationId != null || DEACTIVATED_STATUSES.contains(status);
        return new ChallengerDeactivationListItemInfo(
            row.get(challenger.id),
            deactivationId,
            row.get(member.name),
            row.get(member.nickname),
            row.get(member.profileImageId),
            row.get(challenger.gisuId),
            row.get(member.schoolId),
            row.get(school.name),
            row.get(challenger.part),
            registered ? ChallengerDeactivationListStatus.REGISTERED : ChallengerDeactivationListStatus.PENDING,
            status,
            row.get(challengerDeactivation.reason),
            row.get(challengerDeactivation.createdAt)
        );
    }
}
