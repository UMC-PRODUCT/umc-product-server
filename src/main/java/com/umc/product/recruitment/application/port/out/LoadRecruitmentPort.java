package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.domain.Recruitment;
import java.time.Instant;

public interface LoadRecruitmentPort {
    Recruitment findById(Long recruitmentId);

    RecruitmentDraftInfo findDraftInfoById(Long recruitmentId);

    RecruitmentApplicationFormInfo findApplicationFormInfoById(Long recruitmentId);

    boolean existsOtherOngoingPublishedRecruitment(
            Long schoolId,
            Long excludeRecruitmentId,
            Instant now
    );
}
