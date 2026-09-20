package com.umc.product.common.domain.enums;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerTrack {
    PLAN("기획", 0),
    DESIGN("디자인", 1),
    WEB_PRODUCT_ENGINEER("웹 프로덕트 엔지니어", 2),
    MOBILE_PRODUCT_ENGINEER("모바일 프로덕트 엔지니어", 3),
    INFRA_PLUS("인프라 플러스", 4),
    ;

    private final String displayName;
    private final int sortOrder;

    public boolean isBasic() {
        return this != INFRA_PLUS;
    }

    /**
     * 리크루팅(트랙 기반)에서 챌린저(파트 기반)로 넘어가는 경계에서 트랙을 파트로 매핑한다. 이름이 동일하므로 항등 매핑이며,
     * INFRA_PLUS는 단독 기본 파트가 아니므로 챌린저 생성 경계에서 허용하지 않는다.
     */
    public ChallengerPart toPart() {
        return switch (this) {
            case PLAN -> ChallengerPart.PLAN;
            case DESIGN -> ChallengerPart.DESIGN;
            case WEB_PRODUCT_ENGINEER -> ChallengerPart.WEB_PRODUCT_ENGINEER;
            case MOBILE_PRODUCT_ENGINEER -> ChallengerPart.MOBILE_PRODUCT_ENGINEER;
            case INFRA_PLUS -> throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
        };
    }
}
