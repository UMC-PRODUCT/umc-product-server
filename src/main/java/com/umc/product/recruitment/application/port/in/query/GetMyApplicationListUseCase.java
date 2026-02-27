package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSummaryInfo;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;

public interface GetMyApplicationListUseCase {
    MyApplicationListInfo get(GetMyApplicationListQuery query);

    interface GetInterviewSchedulingSummaryUseCase {
        InterviewSchedulingSummaryInfo get(GetInterviewSchedulingSummaryQuery query);
    }
}
