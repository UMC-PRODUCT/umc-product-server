package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCurriculumQueryService implements GetCurriculumUseCase {

    private final LoadCurriculumPort loadCurriculumPort;

    @Override
    public CurriculumInfo getByActiveGisuAndPart(ChallengerPart part) {
        return loadCurriculumPort.findByActiveGisuAndPart(part)
            .map(CurriculumInfo::from)
            .orElse(null);
    }
}
