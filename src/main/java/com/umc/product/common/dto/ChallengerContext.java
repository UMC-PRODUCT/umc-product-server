package com.umc.product.common.dto;

import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;

/*
* Challenger 컨텍스트
* 내부 공지 등 Challenger 기능에서 사용합니다
* */
public record ChallengerContext(
        Long challengerId,
        Long challengerRoleId,

        /*
        * 자주 사용할 거 같은 거 캐시용 (role, 조직과 해당 조직id)
        */
        RoleType roleType,
        OrganizationType organizationType,
        Long organizationId

) {
    /*
    * 운영진 여부 확인 (공지 작성 권한 확인용 - 일반 챌린저 제외 공지 작성 가능)
    */
    public boolean isStaff() {
        return roleType != RoleType.CHALLENGER;
    }

    /*
    * 조직과 조직 ID를 함께 이용해서 하나의 조직이 나오기 때문에, 이를 위한 메서드 분리
    * ex| OrganizationType.SCHOOL & targetOrgId == 2 -> 특정 학교
    */
    public boolean canAccessOrganization(
            OrganizationType targetOrgType,
            Long targetOrgId
    ) {
        if (this.organizationType == OrganizationType.CENTRAL) {
            return true;
        }
        return this.organizationType == targetOrgType &&
                this.organizationId.equals(targetOrgId);
    }


}
