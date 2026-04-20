package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleParticipantPersistenceAdapter implements
    SaveScheduleParticipantPort,
    DeleteScheduleParticipantPort,
    LoadScheduleParticipantPort {

    private final ScheduleParticipantJpaRepository scheduleParticipantJpaRepository;
    private final ScheduleParticipantQueryRepository scheduleParticipantQueryRepository;

    // ======== SaveScheduleParticipantPort =======
    @Override
    public ScheduleParticipant save(ScheduleParticipant scheduleParticipant) {
        return scheduleParticipantJpaRepository.save(scheduleParticipant);
    }

    @Override
    public List<ScheduleParticipant> saveAll(List<ScheduleParticipant> participants) {
        return scheduleParticipantJpaRepository.saveAll(participants);
    }

    // ======== DeleteScheduleParticipantPort =======
    @Override
    public void deleteAll(List<ScheduleParticipant> participants) {
        scheduleParticipantJpaRepository.deleteAll(participants);
    }

    // ======== LoadScheduleParticipantPort =======
    @Override
    public List<ScheduleParticipant> findAllByScheduleId(Long scheduleId) {
        return scheduleParticipantJpaRepository.findAllByScheduleId(scheduleId);
    }

    @Override
    public Optional<ScheduleParticipant> findByScheduleIdAndMemberId(Long scheduleId, Long memberId) {
        return scheduleParticipantJpaRepository.findByScheduleIdAndMemberId(scheduleId, memberId);
    }

    @Override
    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIds(List<Long> scheduleIds) {
        return scheduleParticipantQueryRepository.findParticipantDetailsByScheduleIds(scheduleIds);
    }

    @Override
    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleId(Long scheduleId) {
        return scheduleParticipantQueryRepository.findParticipantDetailsByScheduleId(scheduleId);
    }

    @Override
    public List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIdAndStatus(Long scheduleId,
                                                                                          AttendanceStatus attendanceStatus) {
        scheduleParticipantQueryRepository.findParticipantDetailsByScheduleIdAndStatus(scheduleId, attendanceStatus);
    }

    @Override
    public Set<Long> findMemberIdsByScheduleId(Long scheduleId) {
        return scheduleParticipantQueryRepository.findMemberIdsByScheduleId(scheduleId);
    }
}
