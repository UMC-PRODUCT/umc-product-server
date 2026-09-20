package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchRow;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.domain.Member;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(ChallengerQueryRepository.class)
@DisplayName("챌린저 검색 파트 정렬")
class ChallengerQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ChallengerQueryRepository repository;

    @Test
    @DisplayName("offset 검색은 기존 파트 다음 웹과 모바일을 정렬하고 운영진과 파트 미지정은 뒤에 둔다")
    void offset_검색은_기존_파트와_신규_파트_순서를_지킨다() {
        // given
        persistChallenger("검색모바일", ChallengerPart.MOBILE_PRODUCT_ENGINEER, 12L);
        persistChallenger("검색웹", ChallengerPart.WEB_PRODUCT_ENGINEER, 11L);
        persistChallenger("검색운영진", ChallengerPart.ADMIN, 20L);
        persistChallenger("검색미지정", null, 19L);
        persistChallenger("검색스프링", ChallengerPart.SPRINGBOOT, 10L);
        persistChallenger("검색노드", ChallengerPart.NODEJS, 10L);
        persistChallenger("검색아이폰", ChallengerPart.IOS, 10L);
        persistChallenger("검색안드로이드", ChallengerPart.ANDROID, 10L);
        persistChallenger("검색기존웹", ChallengerPart.WEB, 10L);
        persistChallenger("검색디자인", ChallengerPart.DESIGN, 10L);
        persistChallenger("검색기획", ChallengerPart.PLAN, 10L);
        em.flush();
        em.clear();

        // when
        var result = repository.pagingSearchWithCounts(searchQuery(), PageRequest.of(0, 20));

        // then
        assertThat(result.rows()).extracting(ChallengerSearchRow::part).containsExactly(
            ChallengerPart.PLAN, ChallengerPart.DESIGN, ChallengerPart.WEB,
            ChallengerPart.ANDROID, ChallengerPart.IOS, ChallengerPart.NODEJS,
            ChallengerPart.SPRINGBOOT, ChallengerPart.WEB_PRODUCT_ENGINEER,
            ChallengerPart.MOBILE_PRODUCT_ENGINEER, ChallengerPart.ADMIN, null
        );
        assertThat(result.partCounts())
            .containsEntry(ChallengerPart.WEB_PRODUCT_ENGINEER, 1L)
            .containsEntry(ChallengerPart.MOBILE_PRODUCT_ENGINEER, 1L);
    }

    @Test
    @DisplayName("커서 검색은 스프링 다음 웹과 모바일로 넘어가며 신규 파트를 누락하지 않는다")
    void 커서_검색은_신규_파트_경계에서_누락하지_않는다() {
        // given
        Challenger mobile = persistChallenger("검색모바일", ChallengerPart.MOBILE_PRODUCT_ENGINEER, 12L);
        Challenger web = persistChallenger("검색웹", ChallengerPart.WEB_PRODUCT_ENGINEER, 11L);
        Challenger spring = persistChallenger("검색스프링", ChallengerPart.SPRINGBOOT, 10L);
        em.flush();
        em.clear();

        // when
        var firstPage = repository.cursorSearchWithCounts(searchQuery(), null, 1);
        var afterSpring = repository.cursorSearchWithCounts(searchQuery(), spring.getId(), 1);
        var afterWeb = repository.cursorSearchWithCounts(searchQuery(), web.getId(), 1);
        var afterMobile = repository.cursorSearchWithCounts(searchQuery(), mobile.getId(), 1);

        // then: 다음 페이지 확인용 한 건을 포함한다.
        assertThat(firstPage.rows()).extracting(ChallengerSearchRow::challengerId)
            .containsExactly(spring.getId(), web.getId());
        assertThat(afterSpring.rows()).extracting(ChallengerSearchRow::challengerId)
            .containsExactly(web.getId(), mobile.getId());
        assertThat(afterWeb.rows()).extracting(ChallengerSearchRow::challengerId)
            .containsExactly(mobile.getId());
        assertThat(afterMobile.rows()).isEmpty();
        assertThat(afterWeb.partCounts())
            .containsEntry(ChallengerPart.WEB_PRODUCT_ENGINEER, 1L)
            .containsEntry(ChallengerPart.MOBILE_PRODUCT_ENGINEER, 1L);
    }

    private SearchChallengerQuery searchQuery() {
        return new SearchChallengerQuery(null, "검색", null, null, null, null, null, null, null);
    }

    private Challenger persistChallenger(String name, ChallengerPart part, Long gisuId) {
        Member member = em.persist(Member.create(name, name, name + "@test.com", null, null));
        if (part == null) {
            return em.persist(Challenger.createWithoutEnrollment(member.getId(), gisuId));
        }
        return em.persist(Challenger.builder()
            .memberId(member.getId())
            .part(part)
            .gisuId(gisuId)
            .build());
    }
}
