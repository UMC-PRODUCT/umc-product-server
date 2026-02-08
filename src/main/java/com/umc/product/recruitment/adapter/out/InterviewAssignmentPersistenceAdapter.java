package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.domain.InterviewAssignment;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewAssignmentPersistenceAdapter implements LoadInterviewAssignmentPort {

    private final InterviewAssignmentRepository interviewAssignmentRepository;

    // ============ LoadInterviewAssignmentPort ============
    @Override
    public Optional<InterviewAssignment> findById(Long assignmentId) {
        return interviewAssignmentRepository.findById(assignmentId);
    }
}
