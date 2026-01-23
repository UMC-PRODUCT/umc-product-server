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
}
