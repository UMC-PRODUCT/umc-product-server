package com.umc.product.common.domain.enums;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerPart {
    PLAN("기획", 0),
    DESIGN("디자인", 1),
    WEB("웹", 2),
    ANDROID("안드로이드", 3),
    IOS("iOS", 4),
    NODEJS("노드", 5),
    SPRINGBOOT("스프링부트", 6),
    ADMIN("운영진", 7),
    ;

    private final String displayName;
    private final int sortOrder;

    public static ChallengerPart from(String part) {
        if (part == null || part.isBlank()) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        }
        return Arrays.stream(ChallengerPart.values())
            .filter(challengerPart -> challengerPart.name().equals(part))
            .findFirst()
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND));


    }
}
