package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewAssignment;
import java.util.Optional;

public interface LoadInterviewAssignmentPort {

    Optional<InterviewAssignment> findById(Long assignmentId);
}
