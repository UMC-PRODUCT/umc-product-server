package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAssignmentJpaRepository extends JpaRepository<InterviewAssignment, Long> {
    boolean existsByRecruitment_IdAndApplication_Id(Long recruitmentId, Long applicationId);
    void deleteAllByRecruitmentId(Long recruitmentId);
}
