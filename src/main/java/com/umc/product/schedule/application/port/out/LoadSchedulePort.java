package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

    /**
     * @param memberId             사용자 memberId
     * @param from                 탐색을 시작할 날짜
     * @param to                   탐색을 끝낼 날짜
     * @param isAttendanceRequired true : policy가 존재하는 일정, false or null : policy가 존재하지 않는 일정
     * @return 일정 목록
     */
    List<Schedule> findMySchedules(Long memberId, Instant from, Instant to, Boolean isAttendanceRequired);

    Optional<Schedule> findByIdWithTags(Long scheduleId);

    /**
     * 역할 기반 운영진 일정 조회
     * <p>
     * 조회 조건 : <br> 1. targetScheduleIds에 포함되면서 (from ~ to) 기간 내 시작하는 일정 <br> 2. targetScheduleIds에 포함되면서 승인
     * 대기(*_PENDING) 상태의 참여자가 있는 일정 (기간 무관) <br> 3. attendanceStatus가 지정된 경우, 해당 상태의 참여자가 있는 일정만 반환<br>
     *
     * @param targetScheduleIds 조회 대상 일정 ID 목록 (역할별로 결정됨)
     * @param from              탐색 시작 날짜
     * @param to                탐색 종료 날짜
     * @param attendanceStatus  출석 상태 필터 (null이면 전체)
     * @return 일정 목록
     */
    List<Schedule> findAdminSchedulesByRole(Set<Long> targetScheduleIds,
                                            Instant from,
                                            Instant to,
                                            AttendanceStatus attendanceStatus);

    /**
     * 특정 사용자가 생성한 일정 ID 목록 조회
     *
     * @param authorMemberId 작성자 memberId
     * @return 일정 ID 목록
     */
    Set<Long> findScheduleIdsByAuthor(Long authorMemberId);
}
