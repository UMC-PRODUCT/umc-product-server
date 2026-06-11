package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.School;
import com.umc.product.support.TestContainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    MemberQueryRepository.class
})
@DisplayName("MemberQueryRepository 검색")
class MemberQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    MemberQueryRepository sut;

    @Test
    @DisplayName("keyword는 학교명만 일치하는 회원을 검색하지 않는다")
    void keyword는_학교명만_일치하는_회원을_검색하지_않는다() {
        // given
        School school = persistSchool("한양대학교 ERICA");
        Member member = persistMember("홍길동", "hong", "hong@test.com", school.getId());
        Challenger challenger = persistChallenger(member.getId(), ChallengerPart.PLAN, 7L);
        em.flush();
        em.clear();

        SearchMemberQuery query = new SearchMemberQuery("ERICA", null, null, null, null);
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        var challengers = sut.searchBy(query, pageable);
        var memberIds = sut.searchMemberIdsBy(query, pageable);

        // then
        assertThat(challengers.getContent())
            .extracting(Challenger::getId)
            .doesNotContain(challenger.getId());
        assertThat(challengers.getTotalElements()).isZero();
        assertThat(memberIds.getContent()).isEmpty();
        assertThat(memberIds.getTotalElements()).isZero();
    }

    private School persistSchool(String name) {
        School school = School.create(name, null);
        em.persist(school);
        return school;
    }

    private Member persistMember(String name, String nickname, String email, Long schoolId) {
        Member member = Member.create(name, nickname, email, schoolId, null);
        em.persist(member);
        return member;
    }

    private Challenger persistChallenger(Long memberId, ChallengerPart part, Long gisuId) {
        Challenger challenger = Challenger.builder()
            .memberId(memberId)
            .part(part)
            .gisuId(gisuId)
            .build();
        em.persist(challenger);
        return challenger;
    }
}
