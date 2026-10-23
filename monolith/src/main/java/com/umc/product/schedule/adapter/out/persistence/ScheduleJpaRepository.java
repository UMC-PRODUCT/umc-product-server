package com.umc.product.schedule.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.schedule.domain.Schedule;

public interface ScheduleJpaRepository extends JpaRepository<Schedule, Long> {
}
