package com.umc.product.test.domain.startingline;

import java.util.EnumMap;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 시딩 프로젝트 하나의 TO. 개발 파트는 Web PE와 Mobile PE 중 정확히 하나이고 1명 이상이다(PROJECT-0255, 0200).
 * 디자인 TO가 0이면 디자인 행을 만들지 않는다.
 */
public record ProjectQuota(int design, ChallengerPart engineerPart, int engineer) {
    public ProjectQuota {
        if (engineerPart != ChallengerPart.WEB_PRODUCT_ENGINEER
            && engineerPart != ChallengerPart.MOBILE_PRODUCT_ENGINEER) {
            throw new IllegalArgumentException("개발 파트는 Web PE 또는 Mobile PE여야 합니다: " + engineerPart);
        }
        if (design < 0 || engineer < 1) {
            throw new IllegalArgumentException("TO가 잘못됐습니다: design=" + design + ", engineer=" + engineer);
        }
    }

    public Map<ChallengerPart, Integer> entries() {
        Map<ChallengerPart, Integer> entries = new EnumMap<>(ChallengerPart.class);
        if (design > 0) {
            entries.put(ChallengerPart.DESIGN, design);
        }
        entries.put(engineerPart, engineer);
        return entries;
    }
}
