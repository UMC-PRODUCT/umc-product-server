package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Application;
import java.util.Optional;

public interface LoadApplicationPort {
    Optional<Application> findByRecruitmentIdAndApplicantId(Long recruitmentId, Long memberId);
}
