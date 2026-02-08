package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewSlotPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewSlotPort;
import com.umc.product.recruitment.domain.InterviewSlot;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewSlotPersistenceAdapter implements LoadInterviewSlotPort, SaveInterviewSlotPort {

    private final InterviewSlotJpaRepository interviewSlotJpaRepository;

    @Override
    public boolean existsByRecruitmentId(Long recruitmentId) {
        return interviewSlotJpaRepository.existsByRecruitment_Id(recruitmentId);
    }

    @Override
    public void saveAll(List<InterviewSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return;
        }
        interviewSlotJpaRepository.saveAll(slots);
    }

    @Override
    public List<InterviewSlot> findByRecruitmentIdAndStartsAtBetween(
        Long recruitmentId,
        Instant startsAtInclusive,
        Instant startsAtExclusive
    ) {
        return interviewSlotJpaRepository
            .findByRecruitmentIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                recruitmentId, startsAtInclusive, startsAtExclusive
            );
    }
}
