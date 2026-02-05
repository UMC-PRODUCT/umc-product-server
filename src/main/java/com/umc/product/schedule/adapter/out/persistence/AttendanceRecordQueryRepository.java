package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.schedule.domain.QAttendanceRecord.attendanceRecord;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
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
        return queryFactory
            .select(Projections.constructor(PendingAttendanceInfo.class,
                attendanceRecord.id,
                member.id,
                member.name,
                member.nickname,
                school.name,
                attendanceRecord.status,
                attendanceRecord.memo,
                attendanceRecord.checkedAt
            ))
            .from(attendanceRecord)
            .join(member).on(member.id.eq(attendanceRecord.memberId))
            .leftJoin(school).on(school.id.eq(member.schoolId))
            .where(
                attendanceRecord.attendanceSheetId.eq(sheetId),
                attendanceRecord.status.in(APPROVAL_PENDING_STATUSES)
            )
            .fetch();
    }
}
