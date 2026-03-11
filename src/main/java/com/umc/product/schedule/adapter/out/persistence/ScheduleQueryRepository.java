package com.umc.product.schedule.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QStudyGroup.studyGroup;
import static com.umc.product.organization.domain.QStudyGroupMember.studyGroupMember;
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

    /**
     * 중앙운영사무국원용: 본인 AttendanceRecord가 있는 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     */
    public List<Schedule> findSchedulesForCentralMember(Long memberId, Long gisuId,
                                                        Long authorChallengerId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .leftJoin(attendanceRecord).on(attendanceRecord.attendanceSheetId.eq(attendanceSheet.id))
            .where(
                attendanceSheet.gisuId.eq(gisuId)
                    .and(attendanceSheet.requiresApproval.isTrue())
                    .and(
                        attendanceRecord.memberId.eq(memberId)
                            .or(schedule.authorChallengerId.eq(authorChallengerId))
                    )
            )
            .fetch();
    }

    /**
     * 학교 회장단용: 본인 학교 구성원이 파트장인 스터디 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     */
    public List<Schedule> findSchedulesForSchoolCore(Long schoolId, Long gisuId,
                                                     Long authorChallengerId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .leftJoin(studyGroup).on(studyGroup.id.eq(schedule.studyGroupId))
            .leftJoin(studyGroupMember).on(
                studyGroupMember.studyGroup.id.eq(studyGroup.id)
                    .and(studyGroupMember.isLeader.isTrue())
            )
            .leftJoin(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
            .leftJoin(member).on(member.id.eq(challenger.memberId))
            .where(
                attendanceSheet.requiresApproval.isTrue()
                    .and(attendanceSheet.gisuId.eq(gisuId))
                    .and(
                        member.schoolId.eq(schoolId)
                            .or(schedule.authorChallengerId.eq(authorChallengerId))
                    )
            )
            .fetch();
    }

    /**
     * 학교 파트장용: 본인이 파트장인 스터디 그룹 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     */
    public List<Schedule> findSchedulesForPartLeader(Long challengerId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .leftJoin(studyGroup).on(studyGroup.id.eq(schedule.studyGroupId))
            .leftJoin(studyGroupMember).on(studyGroupMember.studyGroup.id.eq(studyGroup.id))
            .where(
                attendanceSheet.requiresApproval.isTrue()
                    .and(
                        studyGroupMember.challengerId.eq(challengerId)
                            .and(studyGroupMember.isLeader.isTrue())
                            .or(schedule.authorChallengerId.eq(challengerId))
                    )
            )
            .fetch();
    }

    /**
     * 기타 운영진용: 본인이 생성한 일정만 조회 (requiresApproval=true 조건 포함)
     */
    public List<Schedule> findSchedulesByAuthor(Long authorChallengerId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .where(
                attendanceSheet.requiresApproval.isTrue()
                    .and(schedule.authorChallengerId.eq(authorChallengerId))
            )
            .fetch();
    }
}
