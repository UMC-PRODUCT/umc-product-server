package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import java.util.Optional;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

}
