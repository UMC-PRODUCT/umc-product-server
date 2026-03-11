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
     * 스터디 그룹 ID 목록으로 일정 조회 (파트장용)
     *
     * @param studyGroupIds 스터디 그룹 ID 목록
     * @return 일정 목록
     */
    List<Schedule> findByStudyGroupIdIn(List<Long> studyGroupIds);
}
