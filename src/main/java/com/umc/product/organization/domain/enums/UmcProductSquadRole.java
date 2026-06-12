package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UmcProductSquadRole {
    MEMBER("멤버", 0),
    SQUAD_LEAD("Squad 리드", 1);

    private final String displayName;
    private final int sortOrder;
}
