package com.umc.product.authorization.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;

/**
 * 일단 필요할 것 같은 역할 관련 정보 다 때려박은 DTO
 * <p>
 * 추후 필요 없는 내용 리팩토링 필요
 */
public record RoleAttribute(
    ChallengerRoleType roleType,
    OrganizationType organizationType,
    // CENTRAL이면 null, CHAPTER이면 chapterId, SCHOOL이면 schoolId
    Long organizationId,
    ChallengerPart responsiblePart
) {
    public static RoleAttribute from(ChallengerRole challengerRole) {
        return new RoleAttribute(
            challengerRole.getChallengerRoleType(),
            challengerRole.getOrganizationType(),
            challengerRole.getOrganizationId(),
            challengerRole.getResponsiblePart()
        );
    }
}
