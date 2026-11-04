package com.umc.product.test.domain.roster;

import java.util.ArrayList;
import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * QA 계정 593개의 계획. 11기 명단 583명, superadmin, 10기 기존 QA 계정 9개다.
 */
public record QaAccountPlan(List<Account> accounts) {
    public static final int CURRENT_GENERATION = 11;
    public static final int LEGACY_GENERATION = 10;
    public static final String SUPERADMIN = "superadmin";
    /** 팀 노션의 QA 로그인 문서가 이 도메인의 주소를 쓴다. 더미 시딩용 app.seed.email-domain과 따로 둔다. */
    public static final String EMAIL_DOMAIN = "university.neordinary.com";
    private static final String LEGACY_SCHOOL = "중앙대학교";

    private static final List<LegacyAccount> LEGACY_ACCOUNTS = List.of(
        new LegacyAccount(SUPERADMIN, null, null),
        new LegacyAccount("cau_schoolpresident", null, ChallengerRoleType.SCHOOL_PRESIDENT),
        new LegacyAccount("xenon_chapterpresident", null, ChallengerRoleType.CHAPTER_PRESIDENT),
        new LegacyAccount("cau_plan", ChallengerPart.PLAN, null),
        new LegacyAccount("cau_design", ChallengerPart.DESIGN, null),
        new LegacyAccount("cau_web", ChallengerPart.WEB, null),
        new LegacyAccount("cau_android", ChallengerPart.ANDROID, null),
        new LegacyAccount("cau_ios", ChallengerPart.IOS, null),
        new LegacyAccount("cau_node", ChallengerPart.NODEJS, null),
        new LegacyAccount("cau_springboot", ChallengerPart.SPRINGBOOT, null)
    );

    public QaAccountPlan {
        accounts = List.copyOf(accounts);
    }

    public static QaAccountPlan from(QaRoster roster) {
        List<Account> accounts = new ArrayList<>();
        for (RosterEntry entry : roster.entries()) {
            QaRoster.School school = roster.school(entry.key().school());
            accounts.add(new Account(
                entry.key().email(),
                entry.key().displayName(school.shortName()),
                school.name(),
                List.of(new Enrollment(CURRENT_GENERATION, entry.part(), entry.infra(), entry.roles()))));
        }
        for (int index = 0; index < LEGACY_ACCOUNTS.size(); index++) {
            LegacyAccount legacy = LEGACY_ACCOUNTS.get(index);
            accounts.add(new Account(legacyEmail(legacy.alias()), "QA" + (index + 1), LEGACY_SCHOOL,
                legacy.enrollments()));
        }
        return new QaAccountPlan(accounts);
    }

    public static String legacyEmail(String alias) {
        return alias + "@" + EMAIL_DOMAIN;
    }

    public List<String> emails() {
        return accounts.stream().map(Account::email).toList();
    }

    public record Account(String email, String displayName, String schoolName, List<Enrollment> enrollments) {
        public Account {
            enrollments = List.copyOf(enrollments);
        }
    }

    public record Enrollment(int generation, ChallengerPart part, boolean infra, List<RosterRole> roles) {
        public Enrollment {
            roles = List.copyOf(roles);
        }
    }

    private record LegacyAccount(String alias, ChallengerPart part, ChallengerRoleType role) {
        List<Enrollment> enrollments() {
            if (alias.equals(SUPERADMIN)) {
                return List.of();
            }
            List<RosterRole> roles = role == null ? List.of() : List.of(RosterRole.of(role, null));
            return List.of(new Enrollment(LEGACY_GENERATION, part, false, roles));
        }
    }
}
