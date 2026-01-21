package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleJpaRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllOrderByStatusAndDate();
}
