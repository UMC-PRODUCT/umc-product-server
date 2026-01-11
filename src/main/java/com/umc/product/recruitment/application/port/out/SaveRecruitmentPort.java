package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Recruitment;

public interface SaveRecruitmentPort {
    Recruitment save(Recruitment recruitment);

    void deleteById(Long recruitmentId);
}
