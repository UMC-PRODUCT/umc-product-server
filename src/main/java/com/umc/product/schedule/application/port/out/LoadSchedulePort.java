package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

    List<Schedule> findMySchedulesByMonth(Long memberId, Instant monthStart, Instant nextMonthStart);

    /**
     * ID 목록으로 일정 일괄 조회
     *
     * @param ids 일정 ID 목록
     * @return 일정 목록
     */
    List<Schedule> findAllByIds(List<Long> ids);

    /**
     * 모든 일정 조회
     *
     * @return 일정 목록
     */
    List<Schedule> findAll();

    /**
     * 작성자 챌린저 ID 목록으로 일정 조회
     *
     * @param authorChallengerIds 작성자 챌린저 ID 목록
     * @return 일정 목록
     */
    List<Schedule> findByAuthorChallengerIdIn(List<Long> authorChallengerIds);

    /**
     * 작성자 챌린저 ID 목록으로 일정 조회 (AttendanceSheet가 존재하는 일정만)
     *
     * @param authorChallengerIds 작성자 챌린저 ID 목록
     * @return 출석부가 존재하는 일정 목록
     */
    List<Schedule> findWithSheetByAuthorChallengerIdIn(List<Long> authorChallengerIds);

    /**
     * 특정 기수에서 본인의 AttendanceRecord가 존재하는 일정 조회 (중앙 운영사무국용)
     *
     * @param memberId 회원 ID
     * @param gisuId   기수 ID
     * @return 일정 목록
     */
    List<Schedule> findMySchedulesByGisu(Long memberId, Long gisuId);

    /**
     * ID 로 일정 조회
     *
     * @param scheduleId 일정 ID
     * @return 일정
     */
    Optional<Schedule> findByIdWithTags(Long scheduleId);

    /**
     * 중앙운영사무국원용: 본인 AttendanceRecord가 있는 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     *
     * @param memberId           회원 ID
     * @param gisuId             기수 ID
     * @param authorChallengerId 본인 챌린저 ID
     * @return 일정 목록
     */
    List<Schedule> findSchedulesForCentralMember(Long memberId, Long gisuId, Long authorChallengerId);

    /**
     * 학교 회장단용: 본인 학교 구성원이 파트장인 스터디 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     *
     * @param schoolId           학교 ID
     * @param gisuId             기수 ID
     * @param authorChallengerId 본인 챌린저 ID
     * @return 일정 목록
     */
    List<Schedule> findSchedulesForSchoolCore(Long schoolId, Long gisuId, Long authorChallengerId);

    /**
     * 학교 파트장용: 본인이 파트장인 스터디 그룹 일정 + 본인 생성 일정 (requiresApproval=true 조건 포함)
     *
     * @param challengerId 본인 챌린저 ID
     * @return 일정 목록
     */
    List<Schedule> findSchedulesForPartLeader(Long challengerId);

    /**
     * 기타 운영진용: 본인이 생성한 일정만 조회 (requiresApproval=true 조건 포함)
     *
     * @param authorChallengerId 본인 챌린저 ID
     * @return 일정 목록
     */
    List<Schedule> findSchedulesByAuthor(Long authorChallengerId);
}
