package com.umc.product.common.domain.enums;

import java.util.List;
import java.util.Objects;

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

    /**
     * 기본(대표) 트랙 여부. 부가 트랙인 INFRA_PLUS만 false다. 단일 선택·모집 대상 판정에 쓰인다.
     */
    public boolean isBasic() {
        return this != INFRA_PLUS;
    }

    /**
     * 다른 기본 트랙 위에 추가로만 선택할 수 있는 부가 트랙 여부. 현재는 INFRA_PLUS만 해당한다.
     */
    public boolean isAddOn() {
        return this == INFRA_PLUS;
    }

    /**
     * 이 부가 트랙을 주어진 기본 트랙 위에 얹을 수 있는지 여부. INFRA_PLUS는 개발 트랙(WEB/MOBILE PE)에만 부가된다.
     */
    public boolean canCombineWith(ChallengerTrack basic) {
        return this == INFRA_PLUS
            && (basic == WEB_PRODUCT_ENGINEER || basic == MOBILE_PRODUCT_ENGINEER);
    }

    /**
     * 챌린저가 가질 수 있는 트랙 조합인지 검증한다.
     * <p>
     * 규칙: 비어 있거나(운영진 등 미수강), 또는 기본 트랙 정확히 1개 + 부가 트랙(INFRA_PLUS)은 개발 트랙 위에 최대 1개.
     */
    public static boolean isValidSelection(List<ChallengerTrack> tracks) {
        if (tracks == null) {
            return false;
        }
        if (tracks.isEmpty()) {
            return true;
        }
        if (tracks.stream().anyMatch(Objects::isNull)) {
            return false;
        }
        if (tracks.stream().distinct().count() != tracks.size()) {
            return false;
        }
        List<ChallengerTrack> basics = tracks.stream().filter(ChallengerTrack::isBasic).toList();
        if (basics.size() != 1) {
            return false;
        }
        List<ChallengerTrack> addOns = tracks.stream().filter(ChallengerTrack::isAddOn).toList();
        if (addOns.size() > 1) {
            return false;
        }
        return addOns.isEmpty() || addOns.getFirst().canCombineWith(basics.getFirst());
    }

    public static ChallengerTrack from(ChallengerPart part) {
        if (part == null || part == ChallengerPart.ADMIN) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        }

        return switch (part) {
            case PLAN -> PLAN;
            case DESIGN -> DESIGN;
            case WEB, NODEJS, SPRINGBOOT -> WEB_PRODUCT_ENGINEER;
            case ANDROID, IOS -> MOBILE_PRODUCT_ENGINEER;
            case ADMIN -> throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        };
    }
}
