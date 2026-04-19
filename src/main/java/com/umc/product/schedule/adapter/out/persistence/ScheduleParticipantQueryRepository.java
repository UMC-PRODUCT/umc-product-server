package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.schedule.application.port.v2.out.dto.ScheduleParticipantDetailDto;
import java.util.List;
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
                scheduleParticipant.attendance.isisLocationVerified
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
                scheduleParticipant.attendance.isisLocationVerified
            ))
            .from(scheduleParticipant)
            // Participant -> Member 조인
            .join(member).on(scheduleParticipant.memberId.eq(member.id))
            // Member -> School 조인
            .leftJoin(school).on(member.schoolId.eq(school.id))
            .where(scheduleParticipant.schedule.id.eq(scheduleId))
            .fetch();
    }
}
