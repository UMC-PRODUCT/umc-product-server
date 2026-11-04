package com.umc.product.test.domain.roster;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;
import com.umc.product.test.domain.roster.QaPersonas.Persona;

@DisplayName("QA 시나리오 계정 이전표")
class QaPersonasTest {

    private static final String DOMAIN = "qa.test";
    private final QaRoster roster = new ClasspathQaRosterAdapter().load();

    @Test
    @DisplayName("시나리오가 쓰는 계정은 모두 계정 계획 593개 안에 있다")
    void everyPersonaIsPlannedAccount() {
        List<String> planned = QaAccountPlan.from(roster, DOMAIN).emails();

        assertThat(personas()).allSatisfy(persona ->
            assertThat(planned).contains(persona.email(roster, DOMAIN)));
    }

    @Test
    @DisplayName("옛 별칭은 계획 문서의 명단 계정으로 옮긴다")
    void legacyAliasesMoveToRosterSeats() {
        assertThat(QaPersonas.CAU_SCHOOL_PRESIDENT.email(roster, DOMAIN)).isEqualTo("cau_g11_qa01@qa.test");
        assertThat(QaPersonas.CAU_PLAN_LEADER.email(roster, DOMAIN)).isEqualTo("cau_g11_qa03@qa.test");
        assertThat(QaPersonas.CAU_MOBILE_LEADER.email(roster, DOMAIN)).isEqualTo("cau_g11_qa06@qa.test");
        assertThat(QaPersonas.CAU_WEB_INFRA.email(roster, DOMAIN)).isEqualTo("cau_g11_qa12@qa.test");
        assertThat(QaPersonas.CAU_MOBILE_INFRA.email(roster, DOMAIN)).isEqualTo("cau_g11_qa27@qa.test");
        assertThat(QaPersonas.CENTRAL_PRESIDENT.email(roster, DOMAIN)).isEqualTo("duksung_g11_qa03@qa.test");
        assertThat(QaPersonas.CENTRAL_EDUCATION.email(roster, DOMAIN)).isEqualTo("soongsil_g11_qa49@qa.test");
        assertThat(QaPersonas.SUPERADMIN.email(roster, DOMAIN)).isEqualTo("superadmin@qa.test");
        assertThat(roster.entry(RosterKey.of("cau", 12)).infra()).isTrue();
        assertThat(roster.entry(RosterKey.of("cau", 27)).infra()).isTrue();
    }

    private static List<Persona> personas() {
        return Arrays.stream(QaPersonas.class.getFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == Persona.class)
            .map(QaPersonasTest::read)
            .toList();
    }

    private static Persona read(Field field) {
        try {
            return (Persona) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
