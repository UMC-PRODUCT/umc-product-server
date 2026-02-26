package com.umc.product.recruitment.application.port.in.command.dto;

import java.time.Instant;

public record PublishRecruitmentInfo(
    Long recruitmentId,
    Long formId,
    String status,
    Instant publishedAt
) {
}
