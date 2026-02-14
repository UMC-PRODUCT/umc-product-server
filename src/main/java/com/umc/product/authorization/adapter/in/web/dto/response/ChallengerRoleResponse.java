package com.umc.product.authorization.adapter.in.web.dto.response;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import lombok.Builder;

@Builder
public record ChallengerRoleResponse(
    Long challengerRoleId,
    Long challengerId,
    ChallengerRoleType roleType,
    OrganizationType organizationType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId,
    Long gisu
) {
    public static ChallengerRoleResponse from(ChallengerRoleInfo roleInfo, GisuInfo gisuInfo) {
        return ChallengerRoleResponse.builder()
            .challengerRoleId(roleInfo.id())
            .challengerId(roleInfo.challengerId())
            .roleType(roleInfo.roleType())
            .organizationType(roleInfo.organizationType())
            .organizationId(roleInfo.organizationId())
            .responsiblePart(roleInfo.responsiblePart())
            .gisuId(roleInfo.gisuId())
            .gisu(gisuInfo.generation())
            .build();
    }
}
