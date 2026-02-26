package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSlotsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSlotsInfo;

public interface GetInterviewSchedulingSlotsUseCase {
    InterviewSchedulingSlotsInfo get(GetInterviewSchedulingSlotsQuery query);
}
