package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductTeamRole {
    MEMBER("팀원", 0),
    TEAM_LEADER("팀장", 1),
    GENERAL_MANAGER("팀 총괄단", 2),
    PRODUCT_VICE_LEAD("프로덕트팀 부리드", 3),
    PRODUCT_LEAD("프로덕트팀 리드", 4);

    private final String displayName;
    private final int sortOrder;

    public boolean isAtLeastProductTeamManager() {
        return this == GENERAL_MANAGER || this == PRODUCT_LEAD || this == PRODUCT_VICE_LEAD;
    }
}
