package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import java.util.List;
import java.util.Optional;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

    /**
     * 전체 스케줄 조회 (진행중 우선, 종료된 것 최신순)
     */
    List<Schedule> findAllOrderByStatusAndDate();
}
