package com.umc.product.test.domain.roster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaAccountPlan.SystemRoleGrant;

@DisplayName("QA 명단 밖 계정과 시스템 역할")
class QaSystemRolesTest {

    private static final List<String> STANDALONE = List.of(
        "umcproduct01", "umcproduct02", "umcproduct03", "umcproduct04");

    private final ClasspathQaRosterAdapter adapter = new ClasspathQaRosterAdapter();
    private final QaRoster roster = adapter.load();
    private final QaAccountPlan plan = QaAccountPlan.from(roster, adapter.loadSystemRoles());

    @Test
    @DisplayName("UMC_PRODUCT는 명단 밖 4명과 챌린저 역할 조합별 명단 계정 13명, 10기 계정 1명이 갖는다")
    void umcProductHolders() {
        List<String> standalone = STANDALONE.stream().map(QaAccountPlan::aliasEmail).toList();
        List<String> rosterHolders = List.of(
            RosterKey.of("duksung", 6), RosterKey.of("swu", 10), RosterKey.of("inha", 16),
            RosterKey.of("sungshin", 30), RosterKey.of("gachon", 4), RosterKey.of("dongduk", 5),
            RosterKey.of("hongik", 3), RosterKey.of("ewha", 1), RosterKey.of("dankook", 2),
            RosterKey.of("dongduk", 1), RosterKey.of("cau", 6), RosterKey.of("hongiksj", 13),
            RosterKey.of("mju", 1)).stream().map(RosterKey::email).toList();

        assertThat(holders(MemberSystemRoleType.UMC_PRODUCT))
            .hasSize(18)
            .containsAll(standalone)
            .containsAll(rosterHolders)
            .contains(QaAccountPlan.aliasEmail("cau_springboot"));
        assertThat(holders(MemberSystemRoleType.SUPER_ADMIN))
            .containsExactly(QaAccountPlan.aliasEmail(QaAccountPlan.SUPERADMIN));
    }

    @Test
    @DisplayName("명단 밖 계정 4개는 챌린저 없이 중앙대학교 소속 플덕QA1~4로 계획에 들어간다")
    void standaloneAccountsHaveNoEnrollment() {
        List<Account> standalone = plan.accounts().stream()
            .filter(account -> STANDALONE.stream().map(QaAccountPlan::aliasEmail).toList().contains(account.email()))
            .toList();

        assertThat(standalone).extracting(Account::displayName).containsExactly("플덕QA1", "플덕QA2", "플덕QA3", "플덕QA4");
        assertThat(standalone).allSatisfy(account -> {
            assertThat(account.enrollments()).isEmpty();
            assertThat(account.schoolName()).isEqualTo("중앙대학교");
        });
    }

    @Test
    @DisplayName("시스템 역할을 받는 별칭은 모두 계획된 계정 하나에만 대응한다")
    void everyAliasResolvesToExactlyOnePlannedAccount() {
        List<String> emails = plan.emails();

        assertThat(plan.systemRoles()).allSatisfy(grant ->
            assertThat(Collections.frequency(emails, grant.email())).as(grant.email()).isEqualTo(1));
    }

    @Test
    @DisplayName("계획에 없는 별칭에 시스템 역할을 주면 계획을 만들지 않는다")
    void rejectsUnknownAlias() {
        QaSystemRoles typo = QaSystemRoles.parse(List.of("alias,name"),
            List.of("alias,role", "duksung_g11_qa99,UMC_PRODUCT"));

        assertThatThrownBy(() -> QaAccountPlan.from(roster, typo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duksung_g11_qa99");
    }

    @Test
    @DisplayName("명단 밖 계정 별칭이 이미 있는 계정과 겹치면 계획을 만들지 않는다")
    void rejectsStandaloneAliasCollidingWithPlannedAccount() {
        QaSystemRoles collision = QaSystemRoles.parse(List.of("alias,name", "cau_springboot,플덕QA9"),
            List.of("alias,role"));

        assertThatThrownBy(() -> QaAccountPlan.from(roster, collision))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cau_springboot");
    }

    private List<String> holders(MemberSystemRoleType role) {
        return plan.systemRoles().stream().filter(grant -> grant.role() == role).map(SystemRoleGrant::email).toList();
    }
}
