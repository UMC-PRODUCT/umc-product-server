package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsCommand;

import jakarta.validation.constraints.NotNull;

/**
 * 지원서 시나리오 시딩 Request.
 *
 * @param matchingRoundId 지원할 매칭 차수 ID. 현재 OPEN 상태여야 한다.
 * @param chapterId       대상 지부 ID.
 */
public record SeedProjectApplicationsRequest(
    @NotNull Long matchingRoundId,
    @NotNull Long chapterId
) {

    public SeedProjectApplicationsCommand toCommand() {
        return new SeedProjectApplicationsCommand(matchingRoundId, chapterId);
    }
}
