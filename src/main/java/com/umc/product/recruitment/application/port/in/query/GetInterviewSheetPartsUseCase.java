package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsQuery;

public interface GetInterviewSheetPartsUseCase {
    GetInterviewSheetPartsInfo get(GetInterviewSheetPartsQuery query);
}
