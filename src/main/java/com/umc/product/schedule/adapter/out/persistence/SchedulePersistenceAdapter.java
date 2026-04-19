package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePersistenceAdapter implements
    LoadSchedulePort,
    SaveSchedulePort,
    DeleteSchedulePort {

    private final ScheduleJpaRepository scheduleJpaRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    // ========== LoadSchedulePort ==========

    @Override
    public Optional<Schedule> findById(Long id) {
        return scheduleJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return scheduleJpaRepository.existsById(id);
    }

    @Override
    public List<Schedule> findMySchedules(Long memberId, Instant from, Instant to, Boolean isAttendanceRequired) {
        return scheduleQueryRepository.findMySchedules(memberId, from, to, isAttendanceRequired);
    }

    @Override
    public List<Schedule> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return scheduleJpaRepository.findAllById(ids);
    }

    @Override
    public List<Schedule> findAll() {
        return scheduleJpaRepository.findAll();
    }

    @Override
    public List<Schedule> findByAuthorChallengerIdIn(List<Long> authorChallengerIds) {
        if (authorChallengerIds == null || authorChallengerIds.isEmpty()) {
            return List.of();
        }
        return scheduleJpaRepository.findByAuthorChallengerIdIn(authorChallengerIds);
    }

    @Override
    public List<Schedule> findWithSheetByAuthorChallengerIdIn(List<Long> authorChallengerIds) {
        if (authorChallengerIds == null || authorChallengerIds.isEmpty()) {
            return List.of();
        }
        return scheduleQueryRepository.findWithSheetByAuthorChallengerIdIn(authorChallengerIds);
    }

    @Override
    public List<Schedule> findMySchedulesByGisu(Long memberId, Long gisuId) {
        return scheduleQueryRepository.findMySchedulesByGisu(memberId, gisuId);
    }

    @Override
    public Optional<Schedule> findByIdWithTags(Long scheduleId) {
        return scheduleQueryRepository.findByIdWithTags(scheduleId);
    }

    @Override
    public List<Schedule> findSchedulesForCentralMember(Long memberId, Long gisuId) {
        return scheduleQueryRepository.findSchedulesForCentralMember(memberId, gisuId);
    }

    @Override
    public List<Schedule> findSchedulesForSchoolCore(Long schoolId, Long gisuId,
                                                     Long authorChallengerId) {
        return scheduleQueryRepository.findSchedulesForSchoolCore(
            schoolId, gisuId, authorChallengerId);
    }

    @Override
    public List<Schedule> findSchedulesForPartLeader(Long challengerId, Long gisuId) {
        return scheduleQueryRepository.findSchedulesForPartLeader(challengerId, gisuId);
    }

    @Override
    public List<Schedule> findSchedulesByAuthor(Long authorChallengerId, Long gisuId) {
        return scheduleQueryRepository.findSchedulesByAuthor(authorChallengerId, gisuId);
    }

    // ========== SaveSchedulePort ==========

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleJpaRepository.save(schedule);
    }

    // ========== DeleteSchedulePort ==========
    @Override
    public void delete(Long scheduleId) {
        scheduleJpaRepository.deleteById(scheduleId);
    }
}
