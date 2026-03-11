package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.schedule.domain.QAttendanceRecord.attendanceRecord;
import static com.umc.product.schedule.domain.QAttendanceSheet.attendanceSheet;
import static com.umc.product.schedule.domain.QSchedule.schedule;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.domain.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Schedule> findMySchedulesByMonth(Long memberId, Instant monthStart,
                                                 Instant nextMonthStart) {
        return queryFactory
            .selectDistinct(schedule)
            .from(attendanceRecord)
            .join(attendanceSheet).on(attendanceSheet.id.eq(attendanceRecord.attendanceSheetId))
            .join(schedule).on(schedule.id.eq(attendanceSheet.scheduleId))
            .where(
                attendanceRecord.memberId.eq(memberId),
                schedule.startsAt.goe(monthStart),
                schedule.startsAt.lt(nextMonthStart)
            )
            .orderBy(schedule.startsAt.asc())
            .fetch();
    }

    public List<Schedule> findWithSheetByAuthorChallengerIdIn(List<Long> authorChallengerIds) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .where(schedule.authorChallengerId.in(authorChallengerIds))
            .fetch();
    }

    public List<Schedule> findMySchedulesByGisu(Long memberId, Long gisuId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(attendanceRecord)
            .join(attendanceSheet).on(attendanceSheet.id.eq(attendanceRecord.attendanceSheetId))
            .join(schedule).on(schedule.id.eq(attendanceSheet.scheduleId))
            .where(
                attendanceRecord.memberId.eq(memberId),
                attendanceSheet.gisuId.eq(gisuId)
            )
            .fetch();
    }

    public Optional<Schedule> findByIdWithTags(Long scheduleId) {
        return Optional.ofNullable(queryFactory
            .selectFrom(schedule)
            .leftJoin(schedule.tags).fetchJoin()
            .where(schedule.id.eq(scheduleId))
            .fetchOne());
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null ? schedule.id.gt(cursor) : null;
    }
}
