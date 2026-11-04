package com.umc.product.test.domain.roster;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * 명단의 운영진 역할 한 개. 파트장과 교육국원만 담당 파트를 갖는다.
 */
public record RosterRole(ChallengerRoleType type, ChallengerPart responsiblePart) {
    public RosterRole {
        if (type == null) {
            throw new IllegalArgumentException("역할 종류가 필요합니다.");
        }
        if (requiresResponsiblePart(type) != (responsiblePart != null)) {
            throw new IllegalArgumentException("담당 파트가 역할과 맞지 않습니다: " + type + ":" + responsiblePart);
        }
        if (responsiblePart != null && !responsiblePart.isSelectable()) {
            throw new IllegalArgumentException("선택할 수 없는 담당 파트입니다: " + responsiblePart);
        }
    }

    public static RosterRole of(ChallengerRoleType type, ChallengerPart responsiblePart) {
        return new RosterRole(type, responsiblePart);
    }

    /**
     * {@code CENTRAL_EDUCATION_TEAM_MEMBER:DESIGN} 같은 토큰 하나를 읽는다.
     */
    public static RosterRole parse(String token) {
        String[] parts = token.trim().split(":", -1);
        if (parts.length > 2) {
            throw new IllegalArgumentException("역할 토큰 형식이 잘못됐습니다: " + token);
        }
        ChallengerRoleType type = ChallengerRoleType.valueOf(parts[0]);
        ChallengerPart responsiblePart = parts.length == 2 ? ChallengerPart.valueOf(parts[1]) : null;
        return new RosterRole(type, responsiblePart);
    }

    private static boolean requiresResponsiblePart(ChallengerRoleType type) {
        return type == ChallengerRoleType.SCHOOL_PART_LEADER
            || type == ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER;
    }
}
