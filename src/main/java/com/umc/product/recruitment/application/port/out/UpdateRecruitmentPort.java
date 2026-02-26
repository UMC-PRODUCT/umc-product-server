package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Recruitment;

public interface UpdateRecruitmentPort {
    void updateDraft(Recruitment recruitment);
}
