package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewSlot;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSlotJpaRepository extends JpaRepository<InterviewSlot, Long> {

    boolean existsByRecruitment_Id(Long recruitmentId);

    List<InterviewSlot> findByRecruitmentIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
        Long recruitmentId,
        Instant startInclusive,
        Instant endExclusive
    );

}
