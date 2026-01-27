package com.umc.product.curriculum.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.Curriculum;
import java.util.List;
import java.util.Optional;

public interface LoadCurriculumPort {

    Optional<Curriculum> findById(Long id);

    Optional<Curriculum> findByActiveGisuAndPart(ChallengerPart part);

    boolean existsById(Long id);
}
