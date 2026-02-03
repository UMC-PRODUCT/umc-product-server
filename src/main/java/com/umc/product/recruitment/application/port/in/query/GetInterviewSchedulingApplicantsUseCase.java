package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingApplicantsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingApplicantsInfo;

public interface GetInterviewSchedulingApplicantsUseCase {
    InterviewSchedulingApplicantsInfo get(GetInterviewSchedulingApplicantsQuery query);
}
