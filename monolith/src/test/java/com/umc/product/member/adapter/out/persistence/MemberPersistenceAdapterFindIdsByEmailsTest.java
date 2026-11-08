package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.member.domain.Member;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({MemberPersistenceAdapter.class, MemberQueryRepository.class})
@DisplayName("MemberPersistenceAdapter 이메일 일괄 조회")
class MemberPersistenceAdapterFindIdsByEmailsTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    MemberPersistenceAdapter sut;

    @Test
    @DisplayName("이메일로 회원 ID를 한 번에 찾고 없는 이메일은 결과에서 뺀다")
    void findIdsByEmailsReturnsOnlyExistingMembers() {
        // given
        Member first = persistMember("first@test.com");
        Member second = persistMember("second@test.com");
        persistMember("other@test.com");
        em.flush();
        em.clear();

        // when
        Map<String, Long> result = sut.findIdsByEmails(Set.of("first@test.com", "second@test.com", "none@test.com"));

        // then
        assertThat(result).containsOnly(
            Map.entry("first@test.com", first.getId()),
            Map.entry("second@test.com", second.getId()));
    }

    private Member persistMember(String email) {
        Member member = Member.create("회원", "회원", email, null, null);
        em.persist(member);
        return member;
    }
}
