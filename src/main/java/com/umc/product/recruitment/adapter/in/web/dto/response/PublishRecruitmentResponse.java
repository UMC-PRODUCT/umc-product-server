package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentInfo;
import java.time.Instant;

public record PublishRecruitmentResponse(
        Long recruitmentId,
        Long formId,
        String status,
        Instant publishedAt
) {
    public static PublishRecruitmentResponse from(
            PublishRecruitmentInfo info
    ) {
        return new PublishRecruitmentResponse(
                info.recruitmentId(),
                info.formId(),
                info.status(),
                info.publishedAt()
        );
    }
}
