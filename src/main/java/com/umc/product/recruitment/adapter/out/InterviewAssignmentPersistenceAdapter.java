package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.SaveInterviewAssignmentPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class InterviewAssignmentPersistenceAdapter implements SaveInterviewAssignmentPort {

    private final InterviewAssignmentJpaRepository interviewAssignmentJpaRepository;

    @Override
    public void deleteAllByRecruitmentId(Long recruitmentId) {
        interviewAssignmentJpaRepository.deleteAllByRecruitmentId(recruitmentId);
    }
}
