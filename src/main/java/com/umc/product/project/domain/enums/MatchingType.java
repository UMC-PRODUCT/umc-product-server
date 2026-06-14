package com.umc.product.project.domain.enums;

import java.util.Optional;

import com.umc.product.common.domain.enums.ChallengerPart;

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

    /**
     * 챌린저 파트로부터 매칭 종류를 결정한다.
     * <ul>
     *   <li>{@code DESIGN} -> {@code PLAN_DESIGN}</li>
     *   <li>{@code WEB} / {@code ANDROID} / {@code IOS} / {@code NODEJS} / {@code SPRINGBOOT} -> {@code PLAN_DEVELOPER}</li>
     *   <li>{@code PLAN} / {@code ADMIN} -> {@link Optional#empty()} (지원 대상 아님)</li>
     * </ul>
     */
    public static Optional<MatchingType> fromPart(ChallengerPart part) {
        return switch (part) {
            case DESIGN -> Optional.of(PLAN_DESIGN);
            case WEB, ANDROID, IOS, NODEJS, SPRINGBOOT -> Optional.of(PLAN_DEVELOPER);
            case PLAN, ADMIN -> Optional.empty();
        };
    }
}
