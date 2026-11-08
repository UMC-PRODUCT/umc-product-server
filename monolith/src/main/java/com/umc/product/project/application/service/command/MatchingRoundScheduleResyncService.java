package com.umc.product.project.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.ResyncMatchingRoundScheduleUseCase;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingRoundScheduleResyncService implements ResyncMatchingRoundScheduleUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final ScheduleMatchingRoundDeadlinePort scheduleMatchingRoundDeadlinePort;

    @Override
    public MatchingRoundTimers resync(Long roundId) {
        return loadProjectMatchingRoundPort.findById(roundId)
            .map(scheduleMatchingRoundDeadlinePort::schedule)
            .orElseGet(() -> {
                scheduleMatchingRoundDeadlinePort.cancel(roundId);
                return MatchingRoundTimers.none(roundId);
            });
    }
}
