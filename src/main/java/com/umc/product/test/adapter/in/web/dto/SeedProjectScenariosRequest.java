package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosCommand;
import com.umc.product.test.application.port.in.command.dto.TargetProjectStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 시나리오 시딩 Request.
 *
 * @param targetStatus           도달할 프로젝트 상태
 * @param projectCount           생성할 프로젝트 수
 * @param productOwnerMemberIds  PO 로 사용할 멤버 ID 목록. null 이면 활성 기수 PLAN 챌린저 풀에서 랜덤 픽.
 *                               명시 시 size 가 정확히 {@code projectCount} 와 같아야 한다.
 */
public record SeedProjectScenariosRequest(
    @NotNull TargetProjectStatus targetStatus,
    @Positive int projectCount,
    List<Long> productOwnerMemberIds
) {

    public SeedProjectScenariosCommand toCommand() {
        return new SeedProjectScenariosCommand(targetStatus, projectCount, productOwnerMemberIds);
    }
}
