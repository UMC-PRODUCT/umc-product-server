package com.umc.product.organization.adapter.out.persistence.productteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.ProductTeamGeneration;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamSquad;
import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;
import com.umc.product.support.TestContainersConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    ProductTeamMemberQueryRepository.class,
    ProductTeamGenerationPersistenceAdapter.class,
    ProductTeamMemberPersistenceAdapter.class,
    ProductTeamFunctionalUnitPersistenceAdapter.class,
    ProductTeamFunctionalMembershipPersistenceAdapter.class,
    ProductTeamSquadPersistenceAdapter.class,
    ProductTeamSquadParticipantPersistenceAdapter.class
})
class ProductTeamPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProductTeamGenerationPersistenceAdapter generationAdapter;

    @Autowired
    ProductTeamMemberPersistenceAdapter memberAdapter;

    @Autowired
    ProductTeamFunctionalUnitPersistenceAdapter functionalUnitAdapter;

    @Autowired
    ProductTeamFunctionalMembershipPersistenceAdapter functionalMembershipAdapter;

    @Autowired
    ProductTeamSquadPersistenceAdapter squadAdapter;

    @Autowired
    ProductTeamSquadParticipantPersistenceAdapter squadParticipantAdapter;

    @Test
    void 같은_기수에서_챕터와_파트_멤버십을_동시에_가질_수_있다() {
        ProductTeamGeneration generation = saveGeneration(1L);
        ProductTeamMember member = memberAdapter.save(ProductTeamMember.create(100L, "소개", null));
        ProductTeamFunctionalUnit chapter = saveFunctionalUnit(generation.getId(), ProductTeamFunctionalUnitType.CHAPTER, "CLIENT");
        ProductTeamFunctionalUnit part = saveFunctionalUnit(generation.getId(), ProductTeamFunctionalUnitType.PART, "SERVER");

        functionalMembershipAdapter.saveAll(List.of(
            ProductTeamFunctionalMembership.create(
                member,
                generation.getId(),
                chapter.getId(),
                ProductTeamFunctionalRole.MEMBER,
                ProductTeamPosition.UNSPECIFIED,
                "챕터 운영 지원",
                null
            ),
            ProductTeamFunctionalMembership.create(
                member,
                generation.getId(),
                part.getId(),
                ProductTeamFunctionalRole.PART_LEAD,
                ProductTeamPosition.SERVER_DEVELOPER,
                "API 설계",
                null
            )
        ));

        em.flush();
        em.clear();

        assertThat(functionalMembershipAdapter.listByProductTeamMemberId(member.getId()))
            .extracting(ProductTeamFunctionalMembership::getFunctionalUnitId)
            .containsExactlyInAnyOrder(chapter.getId(), part.getId());
    }

    @Test
    void PRODUCT_LEAD는_기수당_한_명만_둘_수_있다() {
        ProductTeamGeneration generation = saveGeneration(2L);
        ProductTeamFunctionalUnit hq = saveFunctionalUnit(generation.getId(), ProductTeamFunctionalUnitType.PRODUCT_HQ, "HQ");
        ProductTeamMember first = memberAdapter.save(ProductTeamMember.create(101L, "소개", null));
        ProductTeamMember second = memberAdapter.save(ProductTeamMember.create(102L, "소개", null));

        functionalMembershipAdapter.save(ProductTeamFunctionalMembership.create(
            first,
            generation.getId(),
            hq.getId(),
            ProductTeamFunctionalRole.PRODUCT_LEAD,
            ProductTeamPosition.PRODUCT_OWNER,
            "프로덕트팀 리드",
            null
        ));
        assertThatThrownBy(() -> {
            functionalMembershipAdapter.save(ProductTeamFunctionalMembership.create(
                second,
                generation.getId(),
                hq.getId(),
                ProductTeamFunctionalRole.PRODUCT_LEAD,
                ProductTeamPosition.PRODUCT_OWNER,
                "프로덕트팀 리드",
                null
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void SQUAD_LEAD는_Squad당_한_명만_둘_수_있다() {
        ProductTeamSquad squad = squadAdapter.save(ProductTeamSquad.create(
            "RECRUIT",
            "모집 Squad",
            null,
            Instant.parse("2026-02-01T00:00:00Z"),
            Instant.parse("2026-03-01T00:00:00Z"),
            1,
            true
        ));
        ProductTeamMember first = memberAdapter.save(ProductTeamMember.create(103L, "소개", null));
        ProductTeamMember second = memberAdapter.save(ProductTeamMember.create(104L, "소개", null));

        squadParticipantAdapter.save(ProductTeamSquadParticipant.create(
            squad,
            first,
            ProductTeamSquadRole.SQUAD_LEAD,
            ProductTeamPosition.PRODUCT_OWNER,
            "모집 정책",
            null
        ));
        assertThatThrownBy(() -> {
            squadParticipantAdapter.save(ProductTeamSquadParticipant.create(
                squad,
                second,
                ProductTeamSquadRole.SQUAD_LEAD,
                ProductTeamPosition.PRODUCT_OWNER,
                "모집 정책",
                null
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 기간이_겹치는_Squad만_기수별_조회에_포함한다() {
        ProductTeamGeneration generation = saveGeneration(3L);
        ProductTeamSquad overlapping = squadAdapter.save(ProductTeamSquad.create(
            "ONBOARDING",
            "온보딩 Squad",
            null,
            Instant.parse("2026-06-01T00:00:00Z"),
            Instant.parse("2026-07-01T00:00:00Z"),
            1,
            true
        ));
        squadAdapter.save(ProductTeamSquad.create(
            "NO_PERIOD",
            "기간 없는 Squad",
            null,
            null,
            null,
            2,
            true
        ));
        squadAdapter.save(ProductTeamSquad.create(
            "OLD",
            "과거 Squad",
            null,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-02-01T00:00:00Z"),
            3,
            true
        ));

        assertThat(squadAdapter.listOverlapping(generation.getStartAt(), generation.getEndAt()))
            .extracting(ProductTeamSquad::getId)
            .containsExactly(overlapping.getId());
    }

    private ProductTeamGeneration saveGeneration(Long generation) {
        return generationAdapter.save(ProductTeamGeneration.create(
            generation,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            false
        ));
    }

    private ProductTeamFunctionalUnit saveFunctionalUnit(
        Long generationId,
        ProductTeamFunctionalUnitType type,
        String code
    ) {
        return functionalUnitAdapter.save(ProductTeamFunctionalUnit.create(
            generationId,
            null,
            type,
            code,
            code + " 이름",
            null,
            1,
            true
        ));
    }
}
