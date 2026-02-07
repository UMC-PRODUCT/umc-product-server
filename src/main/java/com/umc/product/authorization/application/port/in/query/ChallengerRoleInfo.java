package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import lombok.Builder;

@Builder
public record ChallengerRoleInfo(

    Long id,
    Long challengerId,
    ChallengerRoleType roleType,
    OrganizationType organizationType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {
    public static ChallengerRoleInfo fromEntity(ChallengerRole challengerRole) {
        return ChallengerRoleInfo.builder()
            .id(challengerRole.getId())
            .challengerId(challengerRole.getChallengerId())
            .roleType(challengerRole.getChallengerRoleType())
            .organizationType(challengerRole.getOrganizationType())
            .organizationId(challengerRole.getOrganizationId())
            .responsiblePart(challengerRole.getResponsiblePart())
            .gisuId(challengerRole.getGisuId())
            .build();
    }
}
