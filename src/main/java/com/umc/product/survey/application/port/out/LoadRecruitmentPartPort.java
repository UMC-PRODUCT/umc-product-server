package com.umc.product.survey.application.port.out;

import com.umc.product.recruitment.domain.RecruitmentPart;
import java.util.List;

public interface LoadRecruitmentPartPort {
    List<RecruitmentPart> findByRecruitmentId(Long recruitmentId);
}
