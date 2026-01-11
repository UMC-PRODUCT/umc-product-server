package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Recruitment;
import java.util.Optional;

public interface LoadActiveRecruitmentPort {
    Optional<Recruitment> findActive(Long schoolId, Long gisuId);
}
