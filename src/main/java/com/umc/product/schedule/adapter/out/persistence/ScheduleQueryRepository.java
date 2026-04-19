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
            .join(scheduleParticipant).on(participant.schedule.eq(schedule))
            .where(
                participant.memberId.eq(memberId), // 내가 참여자인 일정
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
     * 중앙운영사무국원용: 본인 AttendanceRecord가 있는 일정 조회
     * <p>
     * [조인 구조]
     * <pre>
     * schedule
     *   └─ (inner join) attendance_sheet  : 출석부가 있는 일정만
     *       └─ (inner join) attendance_record : 출석 기록
     * </pre>
     * <p>
     * [조건] - attendance_sheet.gisu_id = :gisuId (현재 기수) - attendance_sheet.requires_approval = true (승인 필요 출석부) -
     * attendance_record.member_id = :memberId (본인이 출석 대상인 일정)
     * <p>
     * [참고] - 일정 생성자는 무조건 attendance_record에 포함되므로, 본인 생성 일정도 자동으로 조회됨 - 따라서 별도의 author_challenger_id 조건 불필요
     */
    public List<Schedule> findSchedulesForCentralMember(Long memberId, Long gisuId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .join(attendanceRecord).on(attendanceRecord.attendanceSheetId.eq(attendanceSheet.id))
            .where(
                attendanceSheet.gisuId.eq(gisuId)
                    .and(attendanceSheet.requiresApproval.isTrue())
                    .and(attendanceRecord.memberId.eq(memberId))
            )
            .fetch();
    }

    /**
     * 학교 회장단용: 본인 학교 구성원이 파트장인 스터디 일정 + 본인 생성 일정
     * <p>
     * [조인 구조]
     * <pre>
     * schedule
     *   ├─ (inner join) attendance_sheet : 출석부가 있는 일정만
     *   └─ (left join) study_group       : 스터디 그룹 (없을 수도 있음 - studyGroupId nullable)
     *       └─ (left join) study_group_member (is_leader=true) : 스터디 그룹 파트장만
     *           └─ (left join) challenger : 파트장의 챌린저 정보
     *               └─ (left join) member : 파트장의 회원 정보 (school_id 확인용)
     * </pre>
     * <p>
     * [조건] - attendance_sheet.requires_approval = true - attendance_sheet.gisu_id = :gisuId - (member.school_id =
     * :schoolId OR schedule.author_challenger_id = :authorChallengerId) → 파트장이 본인 학교 소속이거나, 본인이 생성한 일정
     * <p>
     * [leftJoin 사용 이유] - schedule.study_group_id가 nullable → 스터디 그룹 없는 일정 존재 - 본인 생성 일정은 스터디 그룹과 무관하게 조회되어야 함 -
     * leftJoin 체인으로 스터디 그룹이 없어도 본인 생성 일정 조회 가능
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
     * 학교 파트장용: 본인이 파트장인 스터디 그룹 일정 + 본인 생성 일정
     * <p>
     * [조인 구조]
     * <pre>
     * schedule
     *   ├─ (inner join) attendance_sheet : 출석부가 있는 일정만
     *   └─ (left join) study_group       : 스터디 그룹 (없을 수도 있음)
     *       └─ (left join) study_group_member : 스터디 그룹 멤버 전체
     * </pre>
     * <p>
     * [조건] - attendance_sheet.requires_approval = true - attendance_sheet.gisu_id = :gisuId -
     * ((study_group_member.challenger_id = :challengerId AND is_leader = true) OR schedule.author_challenger_id =
     * :challengerId) → 본인이 파트장인 스터디 일정이거나, 본인이 생성한 일정
     * <p>
     * [leftJoin 사용 이유] - schedule.study_group_id가 nullable → 스터디 그룹 없는 일정 존재 - 본인 생성 일정은 스터디 그룹과 무관 - leftJoin으로 스터디
     * 그룹이 없어도 본인 생성 일정 조회 가능
     * <p>
     * [주의] - study_group_member 조인 시 is_leader 조건을 ON절이 아닌 WHERE절에서 처리 - 모든 멤버와 조인 후 WHERE에서 파트장 여부 필터링
     */
    public List<Schedule> findSchedulesForPartLeader(Long challengerId, Long gisuId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .leftJoin(studyGroup).on(studyGroup.id.eq(schedule.studyGroupId))
            .leftJoin(studyGroupMember).on(studyGroupMember.studyGroup.id.eq(studyGroup.id))
            .where(
                attendanceSheet.requiresApproval.isTrue()
                    .and(attendanceSheet.gisuId.eq(gisuId))
                    .and(
                        studyGroupMember.challengerId.eq(challengerId)
                            .and(studyGroupMember.isLeader.isTrue())
                            .or(schedule.authorChallengerId.eq(challengerId))
                    )
            )
            .fetch();
    }

    /**
     * 기타 운영진용: 본인이 생성한 일정만 조회
     * <p>
     * [조인 구조]
     * <pre>
     * schedule
     *   └─ (inner join) attendance_sheet : 출석부가 있는 일정만
     * </pre>
     * <p>
     * [조건] - attendance_sheet.requires_approval = true - attendance_sheet.gisu_id = :gisuId -
     * schedule.author_challenger_id = :authorChallengerId → 본인이 생성한 일정만 조회
     * <p>
     * [적용 대상] - 중앙운영사무국원, 학교 회장단, 학교 파트장이 아닌 기타 운영진 - SCHOOL_ETC_ADMIN 등의 역할
     */
    public List<Schedule> findSchedulesByAuthor(Long authorChallengerId, Long gisuId) {
        return queryFactory
            .selectDistinct(schedule)
            .from(schedule)
            .join(attendanceSheet).on(attendanceSheet.scheduleId.eq(schedule.id))
            .where(
                attendanceSheet.requiresApproval.isTrue()
                    .and(attendanceSheet.gisuId.eq(gisuId))
                    .and(schedule.authorChallengerId.eq(authorChallengerId))
            )
            .fetch();
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
            .fetch;
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
