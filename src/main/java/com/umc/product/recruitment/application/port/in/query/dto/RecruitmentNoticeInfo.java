package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record RecruitmentNoticeInfo(
        Long recruitmentId,
        String title,
        String content,
        List<ChallengerPart> parts
) {
}
