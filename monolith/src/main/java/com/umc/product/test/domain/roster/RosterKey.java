package com.umc.product.test.domain.roster;

/**
 * 학교 안 명단 순번. QA 계정의 이메일·이름·순서는 모두 이 키에서 나온다.
 */
public record RosterKey(SchoolCode school, int seq) {
    public RosterKey {
        if (school == null) {
            throw new IllegalArgumentException("학교 코드가 필요합니다.");
        }
        if (seq < 1) {
            throw new IllegalArgumentException("명단 순번은 1 이상이어야 합니다: " + seq);
        }
    }

    public static RosterKey of(String school, int seq) {
        return new RosterKey(SchoolCode.of(school), seq);
    }

    public String email() {
        return "%s_g11_qa%02d@%s".formatted(school.value(), seq, QaAccountPlan.EMAIL_DOMAIN);
    }

    public String displayName(String schoolShortName) {
        return schoolShortName + "QA" + seq;
    }

    @Override
    public String toString() {
        return school.value() + ":" + seq;
    }
}
