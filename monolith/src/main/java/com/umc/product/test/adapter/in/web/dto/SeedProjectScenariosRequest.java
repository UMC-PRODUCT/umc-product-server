package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosCommand;
import com.umc.product.test.application.port.in.command.dto.TargetProjectStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 시나리오 시딩 Request.
 *
 * @param targetStatus          도달할 프로젝트 상태
 * @param projectCount          생성할 프로젝트 수
 * @param productOwnerMemberIds PO 로 사용할 멤버 ID 목록. null 이면 PLAN 챌린저 풀에서 랜덤 픽.
 *                              명시 시 size 가 정확히 {@code projectCount} 와 같아야 한다.
 * @param adminMemberId         {@code IN_PROGRESS} 에서 필수. 프로젝트를 등록·공개할 총괄단 또는 지부장 멤버 ID
 */
public record SeedProjectScenariosRequest(
    @NotNull TargetProjectStatus targetStatus,
    @Positive int projectCount,
    List<Long> productOwnerMemberIds,
    Long adminMemberId
) {

    @AssertTrue(message = "IN_PROGRESS 시나리오에는 adminMemberId 가 필요합니다.") @JsonIgnore
    @Schema(hidden = true)
    public boolean isAdminMemberIdPresentForInProgress() {
        return targetStatus != TargetProjectStatus.IN_PROGRESS || adminMemberId != null;
    }

    public SeedProjectScenariosCommand toCommand() {
        return new SeedProjectScenariosCommand(targetStatus, projectCount, productOwnerMemberIds, adminMemberId);
    }
}
