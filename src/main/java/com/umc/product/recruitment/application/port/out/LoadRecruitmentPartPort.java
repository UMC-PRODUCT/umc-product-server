package com.umc.product.recruitment.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.util.List;

public interface LoadRecruitmentPartPort {
    List<RecruitmentPart> findByRecruitmentId(Long recruitmentId);

    List<RecruitmentPart> findByRecruitmentIdAndStatus(Long recruitmentId, RecruitmentPartStatus status);

    List<ChallengerPart> findOpenPartsByRecruitmentId(Long recruitmentId);

    List<ChallengerPart> findOpenPartsByRootId(Long rootId);
}
