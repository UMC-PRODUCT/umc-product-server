package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.Recruitment;
import java.util.Optional;

public interface LoadRecruitmentPort {
    Optional<Recruitment> findById(Long recruitmentId);
}
