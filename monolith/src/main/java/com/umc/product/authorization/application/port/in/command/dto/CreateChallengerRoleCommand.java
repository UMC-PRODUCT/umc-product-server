package com.umc.product.authorization.application.port.in.command.dto;

import com.umc.product.authorization.adapter.in.web.dto.request.CreateChallengerRoleRequest;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import lombok.Builder;

@Builder
public record CreateChallengerRoleCommand(
    Long challengerId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {
    public static CreateChallengerRoleCommand from(CreateChallengerRoleRequest request) {
        return CreateChallengerRoleCommand.builder()
            .challengerId(request.challengerId())
            .roleType(request.roleType())
            .organizationId(request.organizationId())
            .responsiblePart(request.responsiblePart())
            .gisuId(request.gisuId())
            .build();
    }

    public ChallengerRole toEntity() {
        return ChallengerRole.create(
            challengerId, roleType, organizationId, responsiblePart, gisuId
        );
    }
}
