package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetPartsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSheetQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetQuestionsQuery;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQuestionQueryService implements GetInterviewSheetQuestionsUseCase,
    GetInterviewSheetPartsUseCase {

    private final LoadRecruitmentPartPort loadRecruitmentPartPort;

    @Override
    public GetInterviewSheetQuestionsInfo get(GetInterviewSheetQuestionsQuery query) {
        // partKey가 null이면 common으로 처리
        return null;
    }

    @Override
    public GetInterviewSheetPartsInfo get(GetInterviewSheetPartsQuery query) {
        // 1. status가 OPEN인 모집 파트만 가져오기
        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentIdAndStatus(query.recruitmentId(),
            RecruitmentPartStatus.OPEN);

        // 2. COMMON 맨앞에 추가
        List<PartKey> partKeys = new ArrayList<>();
        partKeys.add(PartKey.COMMON);

        // 3. 이어서 모집하는 파트 추가
        parts.stream()
            .map(RecruitmentPart::getPart)
            .sorted(Comparator.comparingInt(ChallengerPart::getSortOrder)) // 정렬
            .map(challengerPart -> PartKey.valueOf(challengerPart.name()))
            .forEach(partKeys::add);

        return new GetInterviewSheetPartsInfo(partKeys);
    }
}
