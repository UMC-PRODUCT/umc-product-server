package com.umc.product.recruitment.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public enum RecruitmentListStatusQuery {
    @Schema(description = "진행 중인 모집")
    ONGOING,

    @Schema(description = "예정된 모집")
    SCHEDULED,

    @Schema(description = "종료된 모집")
    CLOSED,

    @Schema(description = "임시저장된 모집")
    DRAFT
}
