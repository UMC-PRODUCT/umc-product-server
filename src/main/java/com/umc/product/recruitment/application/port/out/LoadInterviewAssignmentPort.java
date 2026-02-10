package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAssignmentRow;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.domain.InterviewAssignment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadInterviewAssignmentPort {

    long countByRecruitmentId(Long recruitmentId);

    long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    long countByRecruitmentIdAndDateAndFirstPreferredPart(Long recruitmentId, LocalDate date, PartOption part);

    Set<Long> findAssignedApplicationIdsByRecruitmentId(Long recruitmentId);

    List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRecruitmentIdAndSlotId(
        Long recruitmentId,
        Long slotId,
        PartOption part
    );

    boolean existsByRecruitmentIdAndApplicationId(Long recruitmentId, Long applicationId);

    Optional<InterviewAssignment> findById(Long assignmentId);

    List<InterviewAssignment> findByRecruitmentIdWithSlotAndApplication(Long recruitmentId);
}
