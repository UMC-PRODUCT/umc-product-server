package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingAssignmentsInfo;

public interface GetInterviewSchedulingAssignmentsUseCase {
    InterviewSchedulingAssignmentsInfo get(GetInterviewSchedulingAssignmentsQuery query);
}
