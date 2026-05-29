package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsCommand;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * 지원서 시나리오 시딩 Request.
 *
 * @param matchingRoundId 지원할 매칭 차수 ID. 반드시 현재 OPEN 상태여야 한다.
 * @param chapterId       대상 지부 ID.
 * @param approveRatio    SUBMITTED 지원서 중 APPROVED 처리할 비율 (0.0 ~ 1.0). 생략 시 기본값 0.5.
 */
public record SeedProjectApplicationsRequest(
    @NotNull Long matchingRoundId,
    @NotNull Long chapterId,
    @DecimalMin("0.0") @DecimalMax("1.0") Double approveRatio
) {

    public SeedProjectApplicationsRequest {
        if (approveRatio == null) {
            approveRatio = 0.5;
        }
    }

    public SeedProjectApplicationsCommand toCommand() {
        return new SeedProjectApplicationsCommand(matchingRoundId, chapterId, approveRatio);
    }
}
