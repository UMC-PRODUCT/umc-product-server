package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleJpaRepository extends JpaRepository<Schedule, Long> {

}
