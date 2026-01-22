package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.schedule.domain.QAttendanceRecord.attendanceRecord;
import static com.umc.product.schedule.domain.QAttendanceSheet.attendanceSheet;
import static com.umc.product.schedule.domain.QSchedule.schedule;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.domain.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Schedule> findMySchedulesByMonth(Long memberId, LocalDateTime monthStart, LocalDateTime nextMonthStart) {
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
}
