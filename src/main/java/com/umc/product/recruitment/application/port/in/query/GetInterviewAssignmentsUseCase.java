package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsQuery;

public interface GetInterviewAssignmentsUseCase {
    GetInterviewAssignmentsInfo get(GetInterviewAssignmentsQuery query);
}
