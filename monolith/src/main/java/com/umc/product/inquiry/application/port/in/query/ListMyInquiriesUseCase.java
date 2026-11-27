package com.umc.product.inquiry.application.port.in.query;

import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryPageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.MyInquiriesQuery;

public interface ListMyInquiriesUseCase {

    InquirySummaryPageInfo listMine(MyInquiriesQuery query);
}
