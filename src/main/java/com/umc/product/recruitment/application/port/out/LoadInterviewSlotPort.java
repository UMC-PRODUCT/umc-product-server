package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewSlot;
import java.time.Instant;
import java.util.List;

public interface LoadInterviewSlotPort {
    boolean existsByRecruitmentId(Long recruitmentId);

    List<InterviewSlot> findByRecruitmentIdAndStartsAtBetween(
        Long recruitmentId,
        Instant startsAtInclusive,
        Instant startsAtExclusive
    );
}
