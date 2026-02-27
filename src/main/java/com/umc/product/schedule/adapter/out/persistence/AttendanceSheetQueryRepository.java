package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.schedule.domain.QAttendanceSheet.attendanceSheet;
import static com.umc.product.schedule.domain.QSchedule.schedule;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.application.port.out.dto.AttendanceSheetPermissionContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceSheetQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 권한 평가에 필요한 컨텍스트 정보 조회 (sheet → schedule JOIN)
     */
    public Optional<AttendanceSheetPermissionContext> findPermissionContext(Long sheetId) {
        AttendanceSheetPermissionContext result = queryFactory
            .select(Projections.constructor(AttendanceSheetPermissionContext.class,
                attendanceSheet.id,
                attendanceSheet.gisuId,
                schedule.id,
                schedule.authorChallengerId
            ))
            .from(attendanceSheet)
            .join(schedule).on(schedule.id.eq(attendanceSheet.scheduleId))
            .where(attendanceSheet.id.eq(sheetId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
