package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsQuery;

public interface GetInterviewOptionsUseCase {
    GetInterviewOptionsInfo get(GetInterviewOptionsQuery query);
}
