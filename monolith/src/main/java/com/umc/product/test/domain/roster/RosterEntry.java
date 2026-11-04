package com.umc.product.test.domain.roster;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 명단 한 줄. 파트가 없으면 수강하지 않는 운영진이다.
 */
public record RosterEntry(RosterKey key, ChallengerPart part, boolean infra, List<RosterRole> roles) {
    public RosterEntry {
        if (key == null) {
            throw new IllegalArgumentException("명단 키가 필요합니다.");
        }
        if (part != null && !part.isSelectable()) {
            throw new IllegalArgumentException("선택할 수 없는 파트입니다: " + key + " " + part);
        }
        if (infra && (part == null || !part.canHaveInfra())) {
            throw new IllegalArgumentException("인프라 참여는 개발 파트만 가능합니다: " + key);
        }
        roles = List.copyOf(roles);
    }

    public boolean hasPart(ChallengerPart candidate) {
        return part == candidate;
    }

    public boolean isGeneralChallenger() {
        return part != null && roles.isEmpty();
    }
}
