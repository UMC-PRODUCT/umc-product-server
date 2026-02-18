package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.schedule.domain.QAttendanceRecord.attendanceRecord;
import static com.umc.product.schedule.domain.QAttendanceSheet.attendanceSheet;
import static com.umc.product.schedule.domain.QSchedule.schedule;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.dto.AttendanceRecordPermissionContext;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

//추가
@Repository
@RequiredArgsConstructor
public class AttendanceRecordQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final List<AttendanceStatus> APPROVAL_PENDING_STATUSES = List.of(
        AttendanceStatus.PRESENT_PENDING,
        AttendanceStatus.LATE_PENDING,
        AttendanceStatus.EXCUSED_PENDING
    );

    public List<PendingAttendanceInfo> findPendingWithMemberInfo(Long sheetId) {
        return buildPendingBaseQuery()
            .where(
                attendanceRecord.attendanceSheetId.eq(sheetId),
                attendanceRecord.status.in(APPROVAL_PENDING_STATUSES)
            )
            .orderBy(attendanceRecord.checkedAt.desc())
            .fetch();
    }

    /**
     * 여러 출석부의 승인 대기 출석 기록을 멤버 정보와 함께 일괄 조회
     * <p>
     * N+1 방지를 위해 여러 출석부를 한 번에 조회. scheduleId를 함께 조회하여 그룹핑에 활용.
     */
    public List<PendingAttendanceInfo> findPendingWithMemberInfoBySheetIds(List<Long> sheetIds) {
        if (sheetIds == null || sheetIds.isEmpty()) {
            return List.of();
        }

        return buildPendingBaseQuery()
            .where(
                attendanceRecord.attendanceSheetId.in(sheetIds),
                attendanceRecord.status.in(APPROVAL_PENDING_STATUSES)
            )
            .orderBy(attendanceRecord.checkedAt.desc())
            .fetch();
    }

    private JPAQuery<PendingAttendanceInfo> buildPendingBaseQuery() {
        return queryFactory
            .select(Projections.constructor(PendingAttendanceInfo.class,
                attendanceRecord.id,
                schedule.id,
                member.id,
                member.name,
                member.nickname,
                member.profileImageId,
                school.name,
                attendanceRecord.status,
                attendanceRecord.memo,
                attendanceRecord.checkedAt
            ))
            .from(attendanceRecord)
            .join(attendanceSheet).on(attendanceSheet.id.eq(attendanceRecord.attendanceSheetId))
            .join(schedule).on(schedule.id.eq(attendanceSheet.scheduleId))
            .join(member).on(member.id.eq(attendanceRecord.memberId))
            .leftJoin(school).on(school.id.eq(member.schoolId));
    }

    /**
     * 권한 평가에 필요한 컨텍스트 정보 조회 (record → sheet → schedule JOIN)
     */
    public Optional<AttendanceRecordPermissionContext> findPermissionContext(Long recordId) {
        AttendanceRecordPermissionContext result = queryFactory
            .select(Projections.constructor(AttendanceRecordPermissionContext.class,
                attendanceRecord.id,
                attendanceRecord.memberId,
                attendanceSheet.id,
                attendanceSheet.gisuId,
                schedule.id,
                schedule.authorChallengerId
            ))
            .from(attendanceRecord)
            .join(attendanceSheet).on(attendanceSheet.id.eq(attendanceRecord.attendanceSheetId))
            .join(schedule).on(schedule.id.eq(attendanceSheet.scheduleId))
            .where(attendanceRecord.id.eq(recordId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
