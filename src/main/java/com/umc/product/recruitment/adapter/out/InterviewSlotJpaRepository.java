package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewSlot;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewSlotJpaRepository extends JpaRepository<InterviewSlot, Long> {

    boolean existsByRecruitment_Id(Long recruitmentId);

    List<InterviewSlot> findByRecruitmentIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
        Long recruitmentId,
        Instant startInclusive,
        Instant endExclusive
    );

    @Query("SELECT s FROM InterviewSlot s " +
        "JOIN s.recruitment r " +
        "WHERE (r.rootRecruitmentId = :rootId OR r.id = :rootId) " +
        "AND s.startsAt >= :start " +
        "AND s.startsAt < :end " +
        "ORDER BY s.startsAt ASC")
    List<InterviewSlot> findByRootIdAndStartsAtBetween(
        @Param("rootId") Long rootId,
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    void deleteAllByRecruitmentId(Long recruitmentId);
}
