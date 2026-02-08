package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewAssignment;

public interface SaveInterviewAssignmentPort {
    InterviewAssignment save(InterviewAssignment assignment);

    void delete(InterviewAssignment assignment);
}
