package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UmcProductFunctionalRole {
    MEMBER("멤버", 0),
    PART_LEAD("파트 리드", 1),
    CHAPTER_LEAD("챕터 리드", 2),
    UMC_PRODUCT_VICE_LEAD("UMC Product 부리드", 3),
    UMC_PRODUCT_LEAD("UMC Product 리드", 4);

    private final String displayName;
    private final int sortOrder;

    public boolean isUmcProductLead() {
        return this == UMC_PRODUCT_LEAD || this == UMC_PRODUCT_VICE_LEAD;
    }
}
