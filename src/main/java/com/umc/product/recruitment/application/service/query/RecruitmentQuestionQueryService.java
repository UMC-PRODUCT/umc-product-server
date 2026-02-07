package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetPartsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQuestionQueryService implements GetInterviewSheetQuestionsUseCase,
    GetInterviewSheetPartsUseCase {

    @Override
    public GetInterviewSheetQuestionsInfo get(GetInterviewSheetQuestionsQuery query) {
        // partKey가 null이면 common으로 처리
        return null;
    }

    @Override
    public GetInterviewSheetPartsInfo get(GetInterviewSheetPartsQuery query) {
        return null;
    }
}
