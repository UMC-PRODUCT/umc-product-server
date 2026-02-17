package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAssignmentRow;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.domain.InterviewAssignment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadInterviewAssignmentPort {

    long countByRootId(Long rootId);

    long countByRootIdAndFirstPreferredPart(Long recruitmentId, PartOption part);

    long countByRootIdAndDateAndFirstPreferredPart(Long rootId, LocalDate date, PartOption part);

    Set<Long> findAssignedApplicationIdsByRecruitmentId(Long recruitmentId);

    List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRecruitmentIdAndSlotId(
        Long recruitmentId,
        Long slotId,
        PartOption part
    );

    boolean existsByRecruitmentIdAndApplicationId(Long recruitmentId, Long applicationId);

    Optional<InterviewAssignment> findById(Long assignmentId);

    List<InterviewAssignment> findByRecruitmentIdWithSlotAndApplication(Long recruitmentId);

    List<InterviewAssignment> findBySlotIds(List<Long> slotIds);

    boolean existsByApplicationId(Long applicationId);

    Set<Long> findAssignedApplicationIdsByRootId(Long rootId);

    List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRootIdAndSlotId(
        Long rootId,
        Long slotId,
        PartOption part
    );

    boolean existsByRootIdAndApplicationId(Long rootId, Long applicationId);
}
