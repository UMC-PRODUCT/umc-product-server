package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.RecruitmentPart;
import java.util.List;

public interface SaveRecruitmentPartPort {
    List<RecruitmentPart> saveAll(List<RecruitmentPart> parts);

    void deleteAllByRecruitmentId(Long recruitmentId);
}
