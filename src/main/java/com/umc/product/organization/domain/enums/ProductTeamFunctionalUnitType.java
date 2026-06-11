package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductTeamFunctionalUnitType {
    PRODUCT_HQ("Product HQ", 0),
    CHAPTER("챕터", 1),
    PART("파트", 2);

    private final String displayName;
    private final int sortOrder;
}
