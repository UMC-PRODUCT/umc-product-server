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

    // root 기반으로 수정
    @Override
    public long countByRootId(Long recruitmentId) {
        return interviewAssignmentQueryRepository.countByRootId(recruitmentId);
    }

    // root 기반으로 수정
    @Override
    public long countByRootIdAndFirstPreferredPart(Long recruitmentId, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRootIdAndFirstPreferredPart(
            recruitmentId, challengerPart
        );
    }

    @Override
    public long countByRootIdAndDateAndFirstPreferredPart(Long rootId, LocalDate date, PartOption part) {
        ChallengerPart challengerPart = toChallengerPart(part);
        return interviewAssignmentQueryRepository.countByRootIdAndDateAndFirstPreferredPart(
            rootId, date, challengerPart
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

    @Override
    public Set<Long> findAssignedApplicationIdsByRootId(Long rootId) {
        return interviewAssignmentQueryRepository.findAssignedApplicationIdsByRootId(rootId);
    }

    @Override
    public List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRootIdAndSlotId(
        Long rootId,
        Long slotId,
        PartOption part
    ) {
        return interviewAssignmentQueryRepository.findAssignmentRowsByRootIdAndSlotId(
            rootId,
            slotId,
            part
        );
    }

    @Override
    public boolean existsByRootIdAndApplicationId(Long rootId, Long applicationId) {
        return interviewAssignmentQueryRepository.existsByRootIdAndApplicationId(rootId, applicationId);
    }

    @Override
    public List<InterviewAssignment> findByRootIdWithSlotAndApplication(Long rootId) {
        return interviewAssignmentQueryRepository.findByRootIdWithSlotAndApplication(rootId);
    }
}
