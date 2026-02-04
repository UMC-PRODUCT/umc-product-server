package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.query.GetFinalSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetFinalSelectionApplicationListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentFinalSelectionQueryService implements GetFinalSelectionListUseCase {

    @Override
    public FinalSelectionApplicationListInfo get(GetFinalSelectionApplicationListQuery query) {
        return null;
    }
}
