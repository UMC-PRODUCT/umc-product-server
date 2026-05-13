package com.umc.product.analytics.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QChapter.chapter;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QGisu.gisu;
import static com.umc.product.schedule.domain.QSchedule.schedule;
import static com.umc.product.schedule.domain.QScheduleParticipant.scheduleParticipant;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminDashboardAnalyticsQueryRepository {

    private static final QChallengerPoint point = QChallengerPoint.challengerPoint;

    private final JPAQueryFactory queryFactory;
    private final Clock clock = Clock.systemUTC();

    public AdminDashboardSummaryInfo getSummary(AdminAnalyticsScope scope) {
        Instant now = Instant.now(clock);
        Instant weekStart = now.minus(7, ChronoUnit.DAYS);
        Instant prevWeekStart = now.minus(14, ChronoUnit.DAYS);
        Instant monthStart = now.atZone(ZoneId.of("Asia/Seoul"))
            .withDayOfMonth(1)
            .truncatedTo(ChronoUnit.DAYS)
            .toInstant();

        long activeChallengerCount = countChallengers(scope, challenger.status.eq(ChallengerStatus.ACTIVE));
        long newMemberCount = countMembers(scope, member.createdAt.goe(weekStart));
        long prevNewMemberCount = countMembers(
            scope,
            member.createdAt.goe(prevWeekStart).and(member.createdAt.lt(weekStart))
        );
        long activeSchoolCount = countActiveSchools(scope);
        long activeChapterCount = countActiveChapters(scope);
        AdminDashboardSummaryInfo.PointSumInfo monthlyPointSum = monthlyPointSum(scope, monthStart);

        return AdminDashboardSummaryInfo.of(
            activeChallengerCount,
            newMemberCount,
            deltaPercent(newMemberCount, prevNewMemberCount),
            activeSchoolCount,
            activeChapterCount,
            monthlyPointSum,
            challengerStatusDistribution(scope)
        );
    }

    public AdminDashboardActionQueueInfo getActionQueue(AdminAnalyticsScope scope, int riskThreshold) {
        Instant now = Instant.now(clock);
        Instant weekStart = now.minus(7, ChronoUnit.DAYS);
        long pendingAttendanceDecisionCount = countPendingAttendanceDecisions(scope);
        long newRiskMemberCountThisWeek = countNewRiskMembersThisWeek(scope, weekStart, riskThreshold);
        long upcomingGraduationCount = isUpcomingGraduation(scope, now)
            ? countChallengers(scope, challenger.status.eq(ChallengerStatus.ACTIVE))
            : 0L;

        return AdminDashboardActionQueueInfo.of(
            pendingAttendanceDecisionCount,
            newRiskMemberCountThisWeek,
            upcomingGraduationCount
        );
    }

    private long countChallengers(AdminAnalyticsScope scope, Predicate extraCondition) {
        Long count = selectFromChallenger(challenger.id.countDistinct(), scope)
            .where(extraCondition)
            .fetchOne();
        return count != null ? count : 0L;
    }

    private long countMembers(AdminAnalyticsScope scope, Predicate extraCondition) {
        Long count = selectFromChallenger(member.id.countDistinct(), scope)
            .where(extraCondition)
            .fetchOne();
        return count != null ? count : 0L;
    }

    private long countActiveSchools(AdminAnalyticsScope scope) {
        Long count = selectFromChallenger(member.schoolId.countDistinct(), scope)
            .where(challenger.status.eq(ChallengerStatus.ACTIVE))
            .fetchOne();
        return count != null ? count : 0L;
    }

    private long countActiveChapters(AdminAnalyticsScope scope) {
        Long count = selectFromChallenger(chapter.id.countDistinct(), scope)
            .where(challenger.status.eq(ChallengerStatus.ACTIVE))
            .fetchOne();
        return count != null ? count : 0L;
    }

    private AdminDashboardSummaryInfo.PointSumInfo monthlyPointSum(AdminAnalyticsScope scope, Instant monthStart) {
        List<Double> scores = queryFactory
            .select(pointScore(point))
            .from(point)
            .join(point.challenger, challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .where(challengerScopeCondition(scope)
                .and(point.createdAt.goe(monthStart)))
            .fetch();

        long positive = 0L;
        long negative = 0L;
        for (Double score : scores) {
            double value = score != null ? score : 0.0;
            if (value > 0) {
                positive += Math.round(value);
            } else if (value < 0) {
                negative += Math.round(value);
            }
        }
        return AdminDashboardSummaryInfo.PointSumInfo.of(positive, negative);
    }

    private Map<ChallengerStatus, Long> challengerStatusDistribution(AdminAnalyticsScope scope) {
        NumberExpression<Long> statusCount = challenger.id.countDistinct();
        List<Tuple> result = selectFromChallenger(challenger.status, scope)
            .select(challenger.status, statusCount)
            .groupBy(challenger.status)
            .fetch();

        Map<ChallengerStatus, Long> distribution = new EnumMap<>(ChallengerStatus.class);
        for (Tuple row : result) {
            distribution.put(row.get(challenger.status), row.get(statusCount));
        }
        return distribution;
    }

    private long countPendingAttendanceDecisions(AdminAnalyticsScope scope) {
        Long count = queryFactory
            .select(scheduleParticipant.id.countDistinct())
            .from(scheduleParticipant)
            .join(scheduleParticipant.schedule, schedule)
            .join(member).on(member.id.eq(schedule.authorMemberId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(scope.gisuId())))
            .where(scheduleScopeCondition(scope)
                .and(scheduleParticipant.attendance.status.in(
                    AttendanceStatus.PRESENT_PENDING,
                    AttendanceStatus.LATE_PENDING,
                    AttendanceStatus.EXCUSED_PENDING,
                    AttendanceStatus.ABSENT_EXCUSE_PENDING,
                    AttendanceStatus.LATE_EXCUSE_PENDING
                )))
            .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanBuilder scheduleScopeCondition(AdminAnalyticsScope scope) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(chapter.gisu.id.eq(scope.gisuId()));
        if (scope.chapterId() != null) {
            builder.and(chapter.id.eq(scope.chapterId()));
        }
        if (scope.schoolId() != null) {
            builder.and(member.schoolId.eq(scope.schoolId()));
        }
        return builder;
    }

    private long countNewRiskMembersThisWeek(AdminAnalyticsScope scope, Instant weekStart, int riskThreshold) {
        NumberExpression<Double> pointSum = pointScore(point).sum().coalesce(0.0);

        return queryFactory
            .select(challenger.id)
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(point).on(point.challenger.id.eq(challenger.id))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .where(challengerScopeCondition(scope)
                .and(challenger.status.eq(ChallengerStatus.ACTIVE))
                .and(member.createdAt.goe(weekStart)))
            .groupBy(challenger.id)
            .having(pointSum.loe((double) riskThreshold))
            .fetch()
            .size();
    }

    private boolean isUpcomingGraduation(AdminAnalyticsScope scope, Instant now) {
        Long count = queryFactory
            .select(gisu.id.count())
            .from(gisu)
            .where(
                gisu.id.eq(scope.gisuId()),
                gisu.period.endAt.loe(now.plus(14, ChronoUnit.DAYS))
            )
            .fetchOne();
        return count != null && count > 0L;
    }

    private <T> JPAQuery<T> selectFromChallenger(Expression<T> expression, AdminAnalyticsScope scope) {
        return queryFactory
            .select(expression)
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(member.schoolId))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .where(challengerScopeCondition(scope));
    }

    private BooleanBuilder challengerScopeCondition(AdminAnalyticsScope scope) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(challenger.gisuId.eq(scope.gisuId()));
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

    private double deltaPercent(long current, long previous) {
        if (previous == 0L) {
            return current == 0L ? 0.0 : 100.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    private NumberExpression<Double> pointScore(QChallengerPoint targetPoint) {
        return new CaseBuilder()
            .when(targetPoint.pointValue.isNotNull()).then(targetPoint.pointValue.doubleValue())
            .otherwise(pointTypeScore(targetPoint));
    }

    private NumberExpression<Double> pointTypeScore(QChallengerPoint targetPoint) {
        CaseBuilder.Cases<Double, NumberExpression<Double>> caseBuilder = null;
        for (PointType pointType : PointType.values()) {
            if (caseBuilder == null) {
                caseBuilder = new CaseBuilder()
                    .when(targetPoint.type.eq(pointType)).then(pointType.getValue());
            } else {
                caseBuilder = caseBuilder
                    .when(targetPoint.type.eq(pointType)).then(pointType.getValue());
            }
        }

        assert caseBuilder != null;
        return caseBuilder.otherwise(0.0);
    }
}
