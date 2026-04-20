package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.ScheduleParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleParticipantJpaRepository extends JpaRepository<ScheduleParticipant, Long> {

    List<ScheduleParticipant> saveAll(List<ScheduleParticipant> participants);

    void deleteAll(List<ScheduleParticipant> participants);

    List<ScheduleParticipant> findAllByScheduleId(Long scheduleId);

    Optional<ScheduleParticipant> findByScheduleIdAndMemberId(Long scheduleId, Long memberId);
}
