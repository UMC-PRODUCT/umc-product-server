package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

    List<Schedule> findMySchedulesByMonth(Long memberId, LocalDateTime monthStart, LocalDateTime nextMonthStart);

    List<Schedule> findMySchedulesByMonthWithCursor(Long memberId, LocalDateTime monthStart,
                                                    LocalDateTime nextMonthStart, Long cursor, int fetchSize);

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
}
