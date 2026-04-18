package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.ScheduleParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleParticipantJpaRepository extends JpaRepository<ScheduleParticipant, Long> {

    ScheduleParticipant save(ScheduleParticipant participant);

    List<ScheduleParticipant> saveAll(List<ScheduleParticipant> participants);

    void deleteAlldeleteAll(List<ScheduleParticipant> participants);

    List<ScheduleParticipant> findAllByScheduleId(Long scheduleId);


}
