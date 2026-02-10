package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewAssignmentJpaRepository extends JpaRepository<InterviewAssignment, Long> {
    boolean existsByRecruitment_IdAndApplication_Id(Long recruitmentId, Long applicationId);
    void deleteAllByRecruitmentId(Long recruitmentId);

    @Query("""
            select distinct ia
            from InterviewAssignment ia
            join fetch ia.slot s
            join fetch ia.application a
            where s.id in :slotIds
        """)
    List<InterviewAssignment> findAllBySlotIdsFetchJoin(@Param("slotIds") List<Long> slotIds);
}
