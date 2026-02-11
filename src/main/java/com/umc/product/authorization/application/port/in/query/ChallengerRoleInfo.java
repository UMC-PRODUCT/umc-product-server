package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import lombok.Builder;

/**
 * 챌린저의 Role 정보를 담습니다. 일반 챌린저가 아닌 운영진 등에 대한 기록을 포함합니다.
 */
@Builder
public record ChallengerRoleInfo(

    Long id,
    Long challengerId, // 해당 시점의 챌린저 ID
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
