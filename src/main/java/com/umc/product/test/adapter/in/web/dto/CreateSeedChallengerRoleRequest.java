package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleCommand;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record CreateSeedChallengerRoleRequest(
    @NotNull(message = "챌린저 ID는 필수입니다") Long challengerId,

    @NotNull(message = "역할 타입은 필수입니다") ChallengerRoleType roleType,

    Long organizationId,

    ChallengerPart responsiblePart,

    @NotNull(message = "기수 ID는 필수입니다") Long gisuId
) {

    public CreateSeedChallengerRoleCommand toCommand() {
        return CreateSeedChallengerRoleCommand.of(
            challengerId,
            roleType,
            organizationId,
            responsiblePart,
            gisuId
        );
    }

    @AssertTrue(message = "중앙 조직이 아닌 역할은 organizationId가 필수입니다") public boolean isOrganizationIdValid() {
        return roleType == null
            || roleType.organizationType() == OrganizationType.CENTRAL
            || organizationId != null;
    }
}
