package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.schedule.domain.QScheduleParticipant.scheduleParticipant;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.application.port.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleParticipantQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIds(List<Long> scheduleIds) {

        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(ScheduleParticipantDetailDto.class,
                scheduleParticipant.schedule.id,
                member.id,
                member.name,
                member.nickname,
                school.id,
                school.name,
                member.profileImageId,
                scheduleParticipant.attendance.status,
                scheduleParticipant.attendance.excuseReason,
                scheduleParticipant.attendance.locationVerified
            ))
            .from(scheduleParticipant)
            // Participant -> Member 조인
            .join(member).on(scheduleParticipant.memberId.eq(member.id))
            // Member -> School 조인
            .leftJoin(school).on(member.schoolId.eq(school.id))
            // N+1 방지를 위한 IN 쿼리
            .where(scheduleParticipant.schedule.id.in(scheduleIds))
            .fetch();
    }

    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleId(Long scheduleId) {

        if (scheduleId == null) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(ScheduleParticipantDetailDto.class,
                scheduleParticipant.schedule.id,
                member.id,
                member.name,
                member.nickname,
                school.id,
                school.name,
                member.profileImageId,
                scheduleParticipant.attendance.status,
                scheduleParticipant.attendance.excuseReason,
                scheduleParticipant.attendance.locationVerified
            ))
            .from(scheduleParticipant)
            // Participant -> Member 조인
            .join(member).on(scheduleParticipant.memberId.eq(member.id))
            // Member -> School 조인
            .leftJoin(school).on(member.schoolId.eq(school.id))
            .where(scheduleParticipant.schedule.id.eq(scheduleId))
            .fetch();
    }

    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIdAndStatus(Long scheduleId,
                                                                                          AttendanceStatus attendanceStatus) {
        return queryFactory
            .select(Projections.constructor(ScheduleParticipantDetailDto.class,
                scheduleParticipant.schedule.id,
                member.id,
                member.name,
                member.nickname,
                school.id,
                school.name,
                member.profileImageId,
                scheduleParticipant.attendance.status,
                scheduleParticipant.attendance.excuseReason,
                scheduleParticipant.attendance.locationVerified
            ))
            .from(scheduleParticipant)
            // Participant -> Member 조인
            .join(member).on(scheduleParticipant.memberId.eq(member.id))
            // Member -> School 조인
            .leftJoin(school).on(member.schoolId.eq(school.id))
            .where(
                scheduleParticipant.schedule.id.eq(scheduleId),
                // attendanceStatus가 null이면 아래 조건은 무시됨
                attendanceStatus != null ? scheduleParticipant.attendance.status.eq(attendanceStatus) : null
            )
            .fetch();
    }

    public Set<Long> findMemberIdsByScheduleId(Long scheduleId) {
        return new HashSet<>(
            queryFactory
                .select(scheduleParticipant.memberId)
                .from(scheduleParticipant)
                .where(scheduleParticipant.schedule.id.eq(scheduleId))
                .fetch()
        );
    }
}
