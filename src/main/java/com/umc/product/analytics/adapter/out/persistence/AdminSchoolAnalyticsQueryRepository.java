package com.umc.product.analytics.adapter.out.persistence;

import static com.umc.product.analytics.adapter.out.persistence.AdminAnalyticsQueryExpressions.chapterMatchedOrNoMapping;
import static com.umc.product.analytics.adapter.out.persistence.AdminAnalyticsQueryExpressions.pointScore;
import static com.umc.product.authorization.domain.QChallengerRole.challengerRole;
import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QChapter.chapter;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QSchool.school;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.analytics.adapter.out.persistence.row.AdminSchoolSummaryRow;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import com.umc.product.analytics.domain.AdminAnalyticsSort;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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
public class AdminSchoolAnalyticsQueryRepository {

    private static final QChallengerPoint point = QChallengerPoint.challengerPoint;

    private final JPAQueryFactory queryFactory;
    private final Clock clock = Clock.systemUTC();

    public Page<AdminSchoolSummaryInfo> getSchoolSummaries(
        AdminAnalyticsScope scope,
        AdminSchoolSummaryQuery query
    ) {
        Instant weekStart = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        List<AdminSchoolSummaryRow> rows = fetchRows(scope, query, weekStart);
        long total = countSchools(scope, query);
        Set<Long> schoolIds = rows.stream().map(AdminSchoolSummaryRow::schoolId).collect(Collectors.toSet());
        Map<Long, AdminSchoolSummaryInfo.StaffInfo> presidents = fetchStaff(schoolIds, scope.gisuId(),
            ChallengerRoleType.SCHOOL_PRESIDENT);
        Map<Long, AdminSchoolSummaryInfo.StaffInfo> vicePresidents = fetchStaff(schoolIds, scope.gisuId(),
            ChallengerRoleType.SCHOOL_VICE_PRESIDENT);
        Map<Long, Long> assignedPartCounts = fetchAssignedPartCounts(schoolIds, scope.gisuId());

        List<AdminSchoolSummaryInfo> content = rows.stream()
            .map(row -> AdminSchoolSummaryInfo.of(
                row.schoolId(),
                row.schoolName(),
                row.chapterId(),
                row.chapterName(),
                row.activeChallengerCount(),
                presidents.get(row.schoolId()),
                vicePresidents.get(row.schoolId()),
                AdminSchoolSummaryInfo.PartLeaderRatioInfo.of(
                    assignedPartCounts.getOrDefault(row.schoolId(), 0L),
                    row.totalRunningParts()
                ),
                row.averagePointSum(),
                row.riskChallengerCount(),
                row.newMemberCountThisWeek()
            ))
            .toList();

        return new PageImpl<>(content, query.pageable(), total);
    }

    private List<AdminSchoolSummaryRow> fetchRows(
        AdminAnalyticsScope scope,
        AdminSchoolSummaryQuery query,
        Instant weekStart
    ) {
        List<ChallengerSchoolMetric> metrics = fetchChallengerMetrics(scope, query);
        List<AdminSchoolSummaryRow> rows = metrics.stream()
            .collect(Collectors.groupingBy(ChallengerSchoolMetric::school))
            .entrySet()
            .stream()
            .map(entry -> toSummaryRow(entry.getKey(), entry.getValue(), query.riskThreshold(), weekStart))
            .sorted(rowComparator(query.sort()))
            .toList();

        return pageRows(rows, query);
    }

    private List<ChallengerSchoolMetric> fetchChallengerMetrics(
        AdminAnalyticsScope scope,
        AdminSchoolSummaryQuery query
    ) {
        NumberExpression<Double> pointSum = pointScore(point).sum().coalesce(0.0);

        List<Tuple> result = queryFactory
            .select(
                school.id,
                school.name,
                chapter.id,
                chapter.name,
                challenger.id,
                challenger.part,
                member.createdAt,
                pointSum
            )
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .join(school).on(school.id.eq(member.schoolId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(school.id))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .leftJoin(point).on(point.challenger.id.eq(challenger.id))
            .where(scopeCondition(scope)
                .and(challenger.status.eq(ChallengerStatus.ACTIVE))
                .and(schoolNameContains(query.search())))
            .groupBy(
                school.id,
                school.name,
                chapter.id,
                chapter.name,
                challenger.id,
                challenger.part,
                member.createdAt
            )
            .fetch();

        return result.stream()
            .map(row -> new ChallengerSchoolMetric(
                new SchoolSummaryKey(
                    row.get(school.id),
                    row.get(school.name),
                    row.get(chapter.id),
                    row.get(chapter.name)
                ),
                row.get(challenger.id),
                row.get(challenger.part),
                row.get(member.createdAt),
                defaultDouble(row.get(pointSum))
            ))
            .toList();
    }

    private AdminSchoolSummaryRow toSummaryRow(
        SchoolSummaryKey school,
        List<ChallengerSchoolMetric> metrics,
        int riskThreshold,
        Instant weekStart
    ) {
        long activeChallengerCount = metrics.stream()
            .map(ChallengerSchoolMetric::challengerId)
            .distinct()
            .count();
        double averagePointSum = metrics.stream()
            .mapToDouble(ChallengerSchoolMetric::pointSum)
            .average()
            .orElse(0.0);
        long riskChallengerCount = metrics.stream()
            .filter(metric -> metric.pointSum() <= riskThreshold)
            .count();
        long newMemberCountThisWeek = metrics.stream()
            .filter(metric -> !metric.memberCreatedAt().isBefore(weekStart))
            .count();
        long totalRunningParts = metrics.stream()
            .map(ChallengerSchoolMetric::part)
            .distinct()
            .count();

        return new AdminSchoolSummaryRow(
            school.schoolId(),
            school.schoolName(),
            school.chapterId(),
            school.chapterName(),
            activeChallengerCount,
            averagePointSum,
            riskChallengerCount,
            newMemberCountThisWeek,
            totalRunningParts
        );
    }

    private List<AdminSchoolSummaryRow> pageRows(List<AdminSchoolSummaryRow> rows, AdminSchoolSummaryQuery query) {
        long offset = query.pageable().getOffset();
        if (offset >= rows.size()) {
            return List.of();
        }

        int fromIndex = (int) offset;
        int toIndex = Math.min(fromIndex + query.pageable().getPageSize(), rows.size());
        return rows.subList(fromIndex, toIndex);
    }

    private long countSchools(AdminAnalyticsScope scope, AdminSchoolSummaryQuery query) {
        Long count = queryFactory
            .select(school.id.countDistinct())
            .from(challenger)
            .join(member).on(member.id.eq(challenger.memberId))
            .join(school).on(school.id.eq(member.schoolId))
            .leftJoin(chapterSchool).on(chapterSchool.school.id.eq(school.id))
            .leftJoin(chapter).on(chapter.id.eq(chapterSchool.chapter.id)
                .and(chapter.gisu.id.eq(challenger.gisuId)))
            .where(scopeCondition(scope)
                .and(challenger.status.eq(ChallengerStatus.ACTIVE))
                .and(schoolNameContains(query.search())))
            .fetchOne();

        return count != null ? count : 0L;
    }

    private Map<Long, AdminSchoolSummaryInfo.StaffInfo> fetchStaff(
        Set<Long> schoolIds,
        Long gisuId,
        ChallengerRoleType roleType
    ) {
        if (schoolIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> result = queryFactory
            .select(challengerRole.organizationId, challenger.id, member.name)
            .from(challengerRole)
            .join(challenger).on(challenger.id.eq(challengerRole.challengerId))
            .join(member).on(member.id.eq(challenger.memberId))
            .where(
                challengerRole.gisuId.eq(gisuId),
                challengerRole.challengerRoleType.eq(roleType),
                challengerRole.organizationId.in(schoolIds)
            )
            .orderBy(challenger.id.asc())
            .fetch();

        Map<Long, AdminSchoolSummaryInfo.StaffInfo> staffBySchoolId = new HashMap<>();
        for (Tuple row : result) {
            staffBySchoolId.putIfAbsent(
                row.get(challengerRole.organizationId),
                AdminSchoolSummaryInfo.StaffInfo.of(row.get(challenger.id), row.get(member.name))
            );
        }
        return staffBySchoolId;
    }

    private Map<Long, Long> fetchAssignedPartCounts(Set<Long> schoolIds, Long gisuId) {
        if (schoolIds.isEmpty()) {
            return Map.of();
        }

        NumberExpression<Long> assignedPartCount = challengerRole.responsiblePart.countDistinct();
        List<Tuple> result = queryFactory
            .select(challengerRole.organizationId, assignedPartCount)
            .from(challengerRole)
            .where(
                challengerRole.gisuId.eq(gisuId),
                challengerRole.challengerRoleType.eq(ChallengerRoleType.SCHOOL_PART_LEADER),
                challengerRole.organizationId.in(schoolIds),
                challengerRole.responsiblePart.isNotNull()
            )
            .groupBy(challengerRole.organizationId)
            .fetch();

        Map<Long, Long> countsBySchoolId = new HashMap<>();
        for (Tuple row : result) {
            countsBySchoolId.put(row.get(challengerRole.organizationId), row.get(assignedPartCount));
        }
        return countsBySchoolId;
    }

    private BooleanBuilder scopeCondition(AdminAnalyticsScope scope) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(challenger.gisuId.eq(scope.gisuId()));
        // chapter_school 의 다중-기수 중복 행을 dedup → pointSum 평균/risk 카운트 부풀림 방지.
        builder.and(chapterMatchedOrNoMapping(chapterSchool, chapter));
        if (scope.chapterId() != null) {
            builder.and(chapter.id.eq(scope.chapterId()));
        }
        if (scope.schoolId() != null) {
            builder.and(school.id.eq(scope.schoolId()));
        }
        if (scope.responsiblePart() != null) {
            builder.and(challenger.part.eq(scope.responsiblePart()));
        }
        return builder;
    }

    private BooleanExpression schoolNameContains(String search) {
        return search == null || search.isBlank()
            ? null
            : school.name.containsIgnoreCase(search);
    }

    private Comparator<AdminSchoolSummaryRow> rowComparator(AdminAnalyticsSort sort) {
        return switch (sort) {
            case ACTIVE_CHALLENGER_COUNT_DESC -> Comparator
                .comparingLong(AdminSchoolSummaryRow::activeChallengerCount)
                .reversed()
                .thenComparing(AdminSchoolSummaryRow::schoolName);
            case SCHOOL_NAME_ASC -> Comparator.comparing(AdminSchoolSummaryRow::schoolName);
            case AVERAGE_POINT_SUM_ASC -> Comparator
                .comparingDouble(AdminSchoolSummaryRow::averagePointSum)
                .thenComparing(AdminSchoolSummaryRow::schoolName);
            case AVERAGE_POINT_SUM_DESC -> Comparator
                .comparingDouble(AdminSchoolSummaryRow::averagePointSum)
                .reversed()
                .thenComparing(AdminSchoolSummaryRow::schoolName);
            case RISK_CHALLENGER_COUNT_DESC -> Comparator
                .comparingLong(AdminSchoolSummaryRow::riskChallengerCount)
                .reversed()
                .thenComparing(AdminSchoolSummaryRow::schoolName);
            default -> Comparator
                .comparingLong(AdminSchoolSummaryRow::riskChallengerCount)
                .reversed()
                .thenComparing(AdminSchoolSummaryRow::schoolName);
        };
    }

    private double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private record ChallengerSchoolMetric(
        SchoolSummaryKey school,
        Long challengerId,
        ChallengerPart part,
        Instant memberCreatedAt,
        double pointSum
    ) {
    }

    private record SchoolSummaryKey(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String chapterName
    ) {
    }
}
