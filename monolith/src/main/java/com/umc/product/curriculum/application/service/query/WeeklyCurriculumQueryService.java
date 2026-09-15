package com.umc.product.curriculum.application.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.curriculum.application.port.in.query.GetWeeklyCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.WeeklyCurriculumInfo;
import com.umc.product.curriculum.application.port.out.LoadWeeklyCurriculumPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyCurriculumQueryService implements GetWeeklyCurriculumUseCase {

    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;

    @Override
    public WeeklyCurriculumInfo getWeeklyCurriculum(Long weeklyCurriculumId) {

        return WeeklyCurriculumInfo.from(
            loadWeeklyCurriculumPort.getById(weeklyCurriculumId)
        );
    }
}
