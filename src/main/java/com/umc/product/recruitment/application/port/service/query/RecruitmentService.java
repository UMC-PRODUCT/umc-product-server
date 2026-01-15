package com.umc.product.recruitment.application.port.service.query;

import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import org.springframework.stereotype.Service;

@Service
public class RecruitmentService implements GetActiveRecruitmentUseCase {

    @Override
    public ActiveRecruitmentInfo get(GetActiveRecruitmentQuery query) {
        return null;
    }
}
