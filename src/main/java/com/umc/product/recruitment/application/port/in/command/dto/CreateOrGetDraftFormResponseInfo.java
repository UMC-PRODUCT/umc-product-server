package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.survey.application.port.in.query.dto.DraftFormResponseInfo;

public record CreateOrGetDraftFormResponseInfo(
        DraftFormResponseInfo draftFormResponseInfo, // survey 도메인 결과
        boolean created
) {
}
