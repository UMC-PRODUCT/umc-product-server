package com.umc.product.analytics.adapter.out.persistence;

import static com.umc.product.analytics.adapter.out.persistence.AdminAnalyticsQueryExpressions.chapterMatchedOrNoMapping;
import static com.umc.product.analytics.adapter.out.persistence.AdminAnalyticsQueryExpressions.pointScore;
import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QChapter.chapter;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QSchool.school;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.analytics.adapter.out.persistence.row.AdminRiskChallengerRow;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRiskChallengerAnalyticsQueryRepository {

    private static final QChallengerPoint point = QChallengerPoint.challengerPoint;

    private final JPAQueryFactory queryFactory;

    public Page<AdminRiskChallengerInfo> getRiskChallengers(
        AdminAnalyticsScope scope,
        AdminRiskChallengerQuery query
    ) {
        List<AdminRiskChallengerRow> rows = fetchRows(scope, query);
        long total = countRows(scope, query);
        Map<Long, AdminRiskChallengerInfo.LatestNegativePointInfo> latestNegativePoints =
            fetchLatestNegativePoints(rows.stream()
                .map(AdminRiskChallengerRow::challengerId)
                .collect(Collectors.toSet()));

        List<AdminRiskChallengerInfo> content = rows.stream()
            .map(row -> AdminRiskChallengerInfo.of(
                row.challengerId(),
                row.memberId(),
                row.name(),
                row.schoolName(),
                row.part(),
                row.pointSum(),
                latestNegativePoints.get(row.challengerId())
            ))
            .toList();

        return new PageImpl<>(content, query.pageable(), total);
    }

    private List<AdminRiskChallengerRow> fetchRows(AdminAnalyticsScope scope, AdminRiskChallengerQuery query) {
        NumberExpression<Double> pointSum = pointScore(point).sum().coalesce(0.0);

        List<Tuple> result = queryFactory
            .select(challenger.id, challenger.memberId, member.name, school.name, challenger.part, pointSum)
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(school).on(school.id.eq(member.schoolId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .leftJoin(point).on(point.challenger.id.eq(challenger.id))
            .where(scopeCondition(scope).and(challenger.status.eq(ChallengerStatus.ACTIVE)))
            .groupBy(challenger.id, challenger.memberId, member.name, school.name, challenger.part)
            .having(pointSum.loe((double) query.riskThreshold()))
            .orderBy(pointSum.asc(), member.name.asc())
            .offset(query.pageable().getOffset())
            .limit(query.pageable().getPageSize())
            .fetch();

        return result.stream()
            .map(row -> new AdminRiskChallengerRow(
                row.get(challenger.id),
                row.get(challenger.memberId),
                row.get(member.name),
                row.get(school.name),
                row.get(challenger.part),
                defaultDouble(row.get(pointSum))
            ))
            .toList();
    }

    private long countRows(AdminAnalyticsScope scope, AdminRiskChallengerQuery query) {
        NumberExpression<Double> pointSum = pointScore(point).sum().coalesce(0.0);

        // HAVING + GROUP BY 결과의 행 수를 COUNT 로 받으려면 서브쿼리가 필요한데,
        // QueryDSL JPA 백엔드는 FROM 절에 서브쿼리를 허용하지 않는다.
        // 대신 ID 만 select 한 후 자바에서 size 를 계산한다 — 위험군은 통상 수십-수백 건이라 메모리 영향이 작다.
        return queryFactory
            .select(challenger.id)
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .leftJoin(point).on(point.challenger.id.eq(challenger.id))
            .where(scopeCondition(scope).and(challenger.status.eq(ChallengerStatus.ACTIVE)))
            .groupBy(challenger.id)
            .having(pointSum.loe((double) query.riskThreshold()))
            .fetch()
            .size();
    }

    private Map<Long, AdminRiskChallengerInfo.LatestNegativePointInfo> fetchLatestNegativePoints(
        Set<Long> challengerIds
    ) {
        if (challengerIds.isEmpty()) {
            return Map.of();
        }

        QChallengerPoint negativePoint = new QChallengerPoint("negativePoint");
        NumberExpression<Double> score = pointScore(negativePoint);

        List<Tuple> result = queryFactory
            .select(negativePoint.challenger.id, negativePoint.type, negativePoint.createdAt, score)
            .from(negativePoint)
            .where(
                negativePoint.challenger.id.in(challengerIds),
                score.lt(0.0)
            )
            .orderBy(negativePoint.challenger.id.asc(), negativePoint.createdAt.desc(), negativePoint.id.desc())
            .fetch();

        Map<Long, AdminRiskChallengerInfo.LatestNegativePointInfo> latestNegativePoints = new HashMap<>();
        for (Tuple row : result) {
            Long challengerId = row.get(negativePoint.challenger.id);
            latestNegativePoints.putIfAbsent(
                challengerId,
                AdminRiskChallengerInfo.LatestNegativePointInfo.of(
                    row.get(negativePoint.type),
                    row.get(negativePoint.createdAt),
                    defaultDouble(row.get(score))
                )
            );
        }
        return latestNegativePoints;
    }

    private BooleanBuilder scopeCondition(AdminAnalyticsScope scope) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(challenger.gisuId.eq(scope.gisuId()));
        // chapter_school 의 다중-기수 중복 행을 dedup → pointSum 부풀림 방지.
        builder.and(chapterMatchedOrNoMapping(chapterSchool, chapter));
        if (scope.chapterId() != null) {
            builder.and(chapter.id.eq(scope.chapterId()));
        }
        if (scope.schoolId() != null) {
            builder.and(member.schoolId.eq(scope.schoolId()));
        }
        if (scope.responsiblePart() != null) {
            builder.and(challenger.part.eq(scope.responsiblePart()));
        }
        return builder;
    }

    private double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }
}
