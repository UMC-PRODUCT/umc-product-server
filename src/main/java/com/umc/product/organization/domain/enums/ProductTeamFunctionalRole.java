package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductTeamFunctionalRole {
    MEMBER("멤버", 0),
    PART_LEAD("파트 리드", 1),
    CHAPTER_LEAD("챕터 리드", 2),
    PRODUCT_VICE_LEAD("프로덕트팀 부리드", 3),
    PRODUCT_LEAD("프로덕트팀 리드", 4);

    private final String displayName;
    private final int sortOrder;

    public boolean isProductTeamLead() {
        return this == PRODUCT_LEAD || this == PRODUCT_VICE_LEAD;
    }
}
