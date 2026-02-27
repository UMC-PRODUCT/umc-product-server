package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.AdminCurriculumInfo;
import com.umc.product.curriculum.application.port.in.query.GetAdminCurriculumUseCase;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCurriculumQueryService implements GetAdminCurriculumUseCase {

    private final LoadCurriculumPort loadCurriculumPort;

    @Override
    public AdminCurriculumInfo getByActiveGisuAndPart(ChallengerPart part) {
        return loadCurriculumPort.findByActiveGisuAndPart(part)
                .map(AdminCurriculumInfo::from)
                .orElse(null);
    }
}
