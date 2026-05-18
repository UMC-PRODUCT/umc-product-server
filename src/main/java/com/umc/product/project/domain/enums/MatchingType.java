package com.umc.product.project.domain.enums;

import lombok.Getter;

@Getter
public enum MatchingType {
    PLAN_DESIGN("기획-디자인 매칭"),
    PLAN_DEVELOPER("기획-개발자 매칭"),
    ;

    private final String displayName;

    MatchingType(String displayName) {
        this.displayName = displayName;
    }
}
