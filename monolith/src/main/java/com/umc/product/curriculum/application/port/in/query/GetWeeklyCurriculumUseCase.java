package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.curriculum.application.port.in.query.dto.WeeklyCurriculumInfo;

public interface GetWeeklyCurriculumUseCase {

    WeeklyCurriculumInfo getWeeklyCurriculum(Long weeklyCurriculumId);
}
