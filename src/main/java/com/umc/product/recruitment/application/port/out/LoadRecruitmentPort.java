package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Recruitment;
import java.util.Optional;

public interface LoadRecruitmentPort {
    Optional<Recruitment> findById(Long recruitmentId);
}
