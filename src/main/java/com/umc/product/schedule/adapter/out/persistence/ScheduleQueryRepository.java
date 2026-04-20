package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.schedule.domain.QSchedule.schedule;
import static com.umc.product.schedule.domain.QScheduleParticipant.scheduleParticipant;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Schedule> findMySchedules(
        Long memberId,
        Instant from,
        Instant to,
        Boolean isAttendanceRequired
    ) {
        return queryFactory.selectFrom(schedule)
            .join(scheduleParticipant).on(scheduleParticipant.schedule.eq(schedule))
            .where(
                scheduleParticipant.memberId.eq(memberId), // 내가 참여자인 일정
                schedule.startsAt.between(from, to), // from ~ to 사이
                isAttendanceRequiredEq(isAttendanceRequired) // 동적 쿼리
            )
            .orderBy(schedule.startsAt.asc())
            .fetch();
    }

    // 동적 쿼리 처리 헬퍼 메서드
    private BooleanExpression isAttendanceRequiredEq(Boolean isAttendanceRequired) {
        if (Boolean.TRUE.equals(isAttendanceRequired)) {
            return schedule.policy.isNotNull(); // 출석 정책이 있는 일정만
        }
        return null; // 조건 무시하고 전체 일정 조회
    }


    public Optional<Schedule> findByIdWithTags(Long scheduleId) {
        return Optional.ofNullable(queryFactory
            .selectFrom(schedule)
            .leftJoin(schedule.tags).fetchJoin()
            .where(schedule.id.eq(scheduleId))
            .fetchOne());
    }

    public List<Schedule> findAdminSchedules(Instant from, Instant to,
                                             AttendanceStatus attendanceStatus,
                                             Long memberId) {
        return queryFactory
            .selectFrom(schedule)
            // scheduleParticipant -> Schedule 조인
            .leftJoin(scheduleParticipant).on(scheduleParticipant.schedule.id.eq(schedule.id))
            .where(
                // 기본 조건 : 요청한 운영진 본인이 참여하는 일정
                scheduleParticipant.memberId.eq(memberId),

                // 추가 조건 : 기간 필터 or 승인 대기(Pending) 상태 필터
                createDateOrPendingCondition(from, to, attendanceStatus)
            )
            // 중복 제거
            .distinct()
            .fetch();
    }

    // ======= 동적 조건 Helper Method =======

    // 기간 필터, 상태 필터, 승인 대기 건 표시 로직 조합 메서드
    private BooleanExpression createDateOrPendingCondition(Instant from, Instant to,
                                                           AttendanceStatus attendanceStatus) {
        // 기간 필터
        BooleanExpression dateCondition = null;
        if (from != null && to != null) {
            dateCondition = schedule.startsAt.between(from, to);
        } else if (from != null) {
            dateCondition = schedule.startsAt.goe(from);
        } else if (to != null) {
            dateCondition = schedule.startsAt.loe(to);
        }

        // 승인 대기 필터 - 기간 상관 없이 이 상태는 무조건 조회되어야 함
        BooleanExpression pendingCondition = scheduleParticipant.attendance.status.in(
            AttendanceStatus.PRESENT_PENDING,
            AttendanceStatus.LATE_PENDING,
            AttendanceStatus.EXCUSED_PENDING,
            AttendanceStatus.ABSENT_EXCUSE_PENDING,
            AttendanceStatus.LATE_EXCUSE_PENDING
        );

        // 상태 필터 (파라미터로 명시적인 검색 조건이 들어온 경우)
        BooleanExpression statusFilter = null;
        if (attendanceStatus != null) {
            statusFilter = scheduleParticipant.attendance.status.eq(attendanceStatus);
        }

        // ======= 조건 조합 로직 =======

        // 경우 1 : 운영진이 명시적으로 특정 상태를 검색한 경우
        // -> 지정 기간 내 + 해당 상태 ('승인 대기 건 무조건 표시' 로직은 무시)
        if (statusFilter != null) {
            if (dateCondition != null) {
                return dateCondition.and(statusFilter);
            }
        }

        // 경우 2 : 상태 필터 없이 전체 조회를 하는 경우
        // -> 지정 기간 내 OR 승인 대기 상태인 것
        if (dateCondition != null) {
            return dateCondition.or(pendingCondition);
        }

        // 예외 처리, 만약 모든 조건 파라미터가 null로 들어온 경우
        return pendingCondition;
    }
}
