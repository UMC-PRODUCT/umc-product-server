package com.umc.product.test.domain.roster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.domain.MemberNamePolicy;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.member.domain.NicknamePolicy;

public record QaAccountPlan(List<Account> accounts, List<SystemRoleGrant> systemRoles) {
    public static final int CURRENT_GENERATION = 11;
    public static final int LEGACY_GENERATION = 10;
    public static final String SUPERADMIN = "superadmin";
    /** 팀 노션의 QA 로그인 문서가 이 도메인의 주소를 쓴다. 더미 시딩용 app.seed.email-domain과 따로 둔다. */
    public static final String EMAIL_DOMAIN = "university.neordinary.com";
    private static final String OFF_ROSTER_SCHOOL = "중앙대학교";
    private static final int HANGUL_SYLLABLE_COUNT = '힣' - '가' + 1;

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
        systemRoles = List.copyOf(systemRoles);
    }

    public static QaAccountPlan from(QaRoster roster, QaSystemRoles systemRoles) {
        List<Account> accounts = new ArrayList<>();
        for (RosterEntry entry : roster.entries()) {
            QaRoster.School school = roster.school(entry.key().school());
            accounts.add(account(accounts.size(),
                entry.key().email(),
                entry.key().displayName(school.shortName()),
                school.name(),
                List.of(new Enrollment(CURRENT_GENERATION, entry.part(), entry.infra(), entry.roles()))));
        }
        for (int index = 0; index < LEGACY_ACCOUNTS.size(); index++) {
            LegacyAccount legacy = LEGACY_ACCOUNTS.get(index);
            accounts.add(account(accounts.size(), aliasEmail(legacy.alias()), "QA" + (index + 1),
                OFF_ROSTER_SCHOOL, legacy.enrollments()));
        }
        for (QaSystemRoles.StandaloneAccount standalone : systemRoles.standaloneAccounts()) {
            accounts.add(account(accounts.size(), aliasEmail(standalone.alias()), standalone.name(),
                OFF_ROSTER_SCHOOL, List.of()));
        }
        Set<String> emails = new HashSet<>();
        for (Account account : accounts) {
            if (!emails.add(account.email())) {
                throw new IllegalArgumentException("QA 계정 이메일이 중복됐습니다: " + account.email());
            }
        }
        List<SystemRoleGrant> grants = new ArrayList<>();
        for (QaSystemRoles.Grant grant : systemRoles.grants()) {
            SystemRoleGrant resolved = new SystemRoleGrant(aliasEmail(grant.alias()), grant.role());
            if (!emails.contains(resolved.email())) {
                throw new IllegalArgumentException("계획에 없는 계정에 시스템 역할을 줍니다: " + grant.alias());
            }
            if (grants.contains(resolved)) {
                throw new IllegalArgumentException("시스템 역할이 중복됐습니다: " + grant.alias() + "," + grant.role());
            }
            grants.add(resolved);
        }
        return new QaAccountPlan(accounts, grants);
    }

    /**
     * 별칭은 QA 계정 이메일의 앞부분이다. 명단 계정은 {@code cau_g11_qa06}, 10기 계정은 {@code cau_springboot} 꼴이다.
     */
    public static String aliasEmail(String alias) {
        return alias + "@" + EMAIL_DOMAIN;
    }

    public List<String> emails() {
        return accounts.stream().map(Account::email).toList();
    }

    private static Account account(int sequence, String email, String displayName, String schoolName,
        List<Enrollment> enrollments) {
        return new Account(email, displayName, nicknameFromSequence(sequence), schoolName, enrollments);
    }

    private static String nicknameFromSequence(int sequence) {
        char[] nickname = new char[NicknamePolicy.MAX_LENGTH];
        int remaining = sequence;
        for (int index = nickname.length - 1; index >= 0; index--) {
            nickname[index] = (char) ('가' + remaining % HANGUL_SYLLABLE_COUNT);
            remaining /= HANGUL_SYLLABLE_COUNT;
        }
        if (remaining != 0) {
            throw new IllegalArgumentException("QA 계정 순번이 한글 4자로 표현 가능한 범위를 초과했습니다.");
        }
        return String.valueOf(nickname);
    }

    public record Account(String email, String displayName, String nickname, String schoolName,
                          List<Enrollment> enrollments) {
        public Account {
            enrollments = List.copyOf(enrollments);
        }

        public String memberName() {
            return displayName.substring(0, Math.min(displayName.length(), MemberNamePolicy.MAX_LENGTH));
        }
    }

    public record SystemRoleGrant(String email, MemberSystemRoleType role) {
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
