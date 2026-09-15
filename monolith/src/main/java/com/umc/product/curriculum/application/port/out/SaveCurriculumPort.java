package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Curriculum;

public interface SaveCurriculumPort {

    Curriculum save(Curriculum curriculum);

    void delete(Curriculum curriculum);
}
