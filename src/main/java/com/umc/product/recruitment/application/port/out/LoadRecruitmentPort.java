package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.domain.Recruitment;

public interface LoadRecruitmentPort {
    Recruitment findById(Long recruitmentId);

    RecruitmentDraftInfo findDraftInfoById(Long recruitmentId);
}
