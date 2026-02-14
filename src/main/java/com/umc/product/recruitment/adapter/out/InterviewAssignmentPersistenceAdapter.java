package com.umc.product.recruitment.adapter.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAssignmentRow;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewAssignmentPort;
import com.umc.product.recruitment.domain.InterviewAssignment;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class InterviewAssignmentPersistenceAdapter implements LoadInterviewAssignmentPort, SaveInterviewAssignmentPort {

    private final InterviewAssignmentQueryRepository interviewAssignmentQueryRepository;
    private final InterviewAssignmentJpaRepository interviewAssignmentJpaRepository;

    @Override
    public long countByRecruitmentId(Long recruitmentId) {
        return interviewAssignmentQueryRepository.countByRecruitmentId(recruitmentId);
    }

    @Override
    public long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRecruitmentIdAndFirstPreferredPart(
            recruitmentId, challengerPart
        );
    }

    @Override
    public long countByRecruitmentIdAndDateAndFirstPreferredPart(Long recruitmentId, LocalDate date, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRecruitmentIdAndDateAndFirstPreferredPart(
            recruitmentId, date, challengerPart
        );
    }

    @Override
    public Set<Long> findAssignedApplicationIdsByRecruitmentId(Long recruitmentId) {
        return interviewAssignmentQueryRepository.findAssignedApplicationIdsByRecruitmentId(recruitmentId);
    }

    private ChallengerPart toChallengerPart(PartOption part) {
        if (part == null || part == PartOption.ALL) {
            throw new IllegalArgumentException("ALL/null is not allowed here");
        }
        return ChallengerPart.valueOf(part.name());
    }

    @Override
    public List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRecruitmentIdAndSlotId(
        Long recruitmentId,
        Long slotId,
        PartOption part
    ) {
        return interviewAssignmentQueryRepository.findAssignmentRowsByRecruitmentIdAndSlotId(recruitmentId, slotId,
            part);
    }

    @Override
    public boolean existsByRecruitmentIdAndApplicationId(Long recruitmentId, Long applicationId) {
        return interviewAssignmentJpaRepository.existsByRecruitment_IdAndApplication_Id(recruitmentId, applicationId);
    }

    @Override
    public InterviewAssignment save(InterviewAssignment assignment) {
        return interviewAssignmentJpaRepository.save(assignment);
    }

    // ============ LoadInterviewAssignmentPort ============
    @Override
    public Optional<InterviewAssignment> findById(Long assignmentId) {
        return interviewAssignmentJpaRepository.findById(assignmentId);
    }

    @Override
    public List<InterviewAssignment> findByRecruitmentIdWithSlotAndApplication(Long recruitmentId) {
        return interviewAssignmentQueryRepository.findByRecruitmentIdWithSlotAndApplication(recruitmentId);
    }

    @Override
    public void delete(InterviewAssignment assignment) {
        interviewAssignmentJpaRepository.delete(assignment);
    }

    @Override
    public void deleteAllByRecruitmentId(Long recruitmentId) {
        interviewAssignmentJpaRepository.deleteAllByRecruitmentId(recruitmentId);
    }

    @Override
    public List<InterviewAssignment> findBySlotIds(List<Long> slotIds) {
        if (slotIds == null || slotIds.isEmpty()) {
            return List.of();
        }
        return interviewAssignmentJpaRepository.findAllBySlotIdsFetchJoin(slotIds);
    }

    @Override
    public boolean existsByApplicationId(Long applicationId) {
        return interviewAssignmentQueryRepository.existsByApplicationId(applicationId);
    }
}
