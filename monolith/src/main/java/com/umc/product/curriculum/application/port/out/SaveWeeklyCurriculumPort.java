package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.WeeklyCurriculum;

public interface SaveWeeklyCurriculumPort {

    WeeklyCurriculum save(WeeklyCurriculum weeklyCurriculum);

    void delete(WeeklyCurriculum weeklyCurriculum);
}