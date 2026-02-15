package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
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
    Long gisuId,
    Long gisu
) {
    @Deprecated(since = "2026-02-16", forRemoval = true)
    public static ChallengerRoleInfo from(ChallengerRole challengerRole) {
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

    public static ChallengerRoleInfo from(ChallengerRole challengerRole, GisuInfo gisuInfo) {
        return ChallengerRoleInfo.builder()
            .id(challengerRole.getId())
            .challengerId(challengerRole.getChallengerId())
            .roleType(challengerRole.getChallengerRoleType())
            .organizationType(challengerRole.getOrganizationType())
            .organizationId(challengerRole.getOrganizationId())
            .responsiblePart(challengerRole.getResponsiblePart())
            .gisuId(challengerRole.getGisuId())
            .gisu(gisuInfo.generation())
            .build();
    }
}
