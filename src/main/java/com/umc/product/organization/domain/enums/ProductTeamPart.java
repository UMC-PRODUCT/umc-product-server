package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductTeamPart {
    HEADQUARTER("프로덕트팀 본부", 0),
    FRONTEND("프론트엔드팀", 1),
    DESIGN("디자인팀", 2),
    SERVER("서버팀", 3),
    MOBILE("모바일팀", 4);

    private final String displayName;
    private final int sortOrder;
}
