package com.umc.product.term.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.support.PersistenceAdapterTest;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;

@PersistenceAdapterTest
@Import({
    TermPersistenceAdapter.class, TermQueryRepository.class})
@DisplayName("TermPersistenceAdapter")
class TermPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    TermPersistenceAdapter sut;

    @Test
    @DisplayName("listActive는 활성 약관만 ID 오름차순으로 반환한다")
    void listActive는_활성_약관만_ID_오름차순으로_반환한다() {
        // given
        Term serviceTerm = persistTerm(TermType.SERVICE, true, true);
        Term privacyTerm = persistTerm(TermType.PRIVACY, true, true);
        Term inactiveTerm = persistTerm(TermType.MARKETING, false, false);
        em.flush();
        em.clear();

        // when
        List<Term> result = sut.listActive();

        // then
        List<Long> resultIds = result.stream()
            .map(Term::getId)
            .toList();

        assertThat(resultIds)
            .contains(serviceTerm.getId(), privacyTerm.getId())
            .doesNotContain(inactiveTerm.getId())
            .isSortedAccordingTo(Comparator.naturalOrder());
        assertThat(result)
            .extracting(Term::isActive)
            .containsOnly(true);
    }

    private Term persistTerm(TermType type, boolean required, boolean active) {
        Term term = Term.builder()
            .type(type)
            .link("https://example.com/terms/" + type.name().toLowerCase())
            .required(required)
            .build();
        if (!active) {
            term.deactivate();
        }
        return em.persist(term);
    }
}
