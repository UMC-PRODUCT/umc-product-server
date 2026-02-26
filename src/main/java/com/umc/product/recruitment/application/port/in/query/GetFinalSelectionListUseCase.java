package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetFinalSelectionApplicationListQuery;

public interface GetFinalSelectionListUseCase {
    FinalSelectionApplicationListInfo get(GetFinalSelectionApplicationListQuery query);
}
