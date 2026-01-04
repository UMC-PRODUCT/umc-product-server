package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.RecruitmentPart;
import java.util.List;

public interface LoadRecruitmentPartPort {
    List<RecruitmentPart> findByRecruitmentId(Long recruitmentId);
}
